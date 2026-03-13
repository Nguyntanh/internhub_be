package com.example.internhub_be.service;

import com.example.internhub_be.domain.FinalEvaluation;
import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.request.FinalEvaluationRequest;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;
import com.example.internhub_be.repository.FinalEvaluationRepository;
import com.example.internhub_be.repository.MicroTaskRepository;
import com.example.internhub_be.repository.TaskSkillRatingRepository;
import com.example.internhub_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinalEvaluationServiceImpl implements FinalEvaluationService {

    private final FinalEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final MicroTaskRepository microTaskRepository;
    private final TaskSkillRatingRepository ratingRepository;

    // ─── GET ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public FinalEvaluationResponse getEvaluationByIntern(Long internId) {
        // Kiểm tra intern tồn tại
        userRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", internId));

        // Lấy đánh giá nếu đã có, nếu chưa vẫn trả về bảng điểm
        Optional<FinalEvaluation> evalOpt = evaluationRepository.findByInternId(internId);

        FinalEvaluationResponse response = new FinalEvaluationResponse();
        response.setInternId(internId);

        evalOpt.ifPresent(eval -> {
            response.setId(eval.getId());
            response.setMentorId(eval.getMentor().getId());
            response.setMentorName(eval.getMentor().getName());
            response.setInternName(eval.getIntern().getName());
            response.setInternEmail(eval.getIntern().getEmail());
            response.setOverallComment(eval.getOverallComment());
            response.setStatus(eval.getStatus().name());
            response.setIsLocked(eval.getIsLocked());
            response.setSubmittedAt(eval.getSubmittedAt());
            response.setCreatedAt(eval.getCreatedAt());
        });

        if (evalOpt.isEmpty()) {
            userRepository.findById(internId).ifPresent(intern -> {
                response.setInternName(intern.getName());
                response.setInternEmail(intern.getEmail());
            });
            response.setStatus(null);
            response.setIsLocked(false);
        }

        // Xây dựng bảng điểm tổng hợp từ task_skill_ratings
        buildSkillSummaries(internId, response);

        return response;
    }

    // ─── SAVE / UPDATE DRAFT ─────────────────────────────────────────────────

    @Override
    @Transactional
    public FinalEvaluationResponse saveOrUpdateDraft(FinalEvaluationRequest request, String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found: " + mentorEmail));

        User intern = userRepository.findById(request.getInternId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getInternId()));

        // Tìm đánh giá đã có hoặc tạo mới
        FinalEvaluation evaluation = evaluationRepository.findByInternId(request.getInternId())
                .orElse(new FinalEvaluation());

        // Không cho phép chỉnh sửa sau khi đã SUBMITTED
        if (evaluation.getId() != null && evaluation.getStatus() == FinalEvaluation.EvaluationStatus.SUBMITTED) {
            throw new IllegalStateException("Đánh giá đã được gửi phê duyệt, không thể chỉnh sửa.");
        }

        evaluation.setIntern(intern);
        evaluation.setMentor(mentor);
        evaluation.setOverallComment(request.getOverallComment());
        evaluation.setStatus(FinalEvaluation.EvaluationStatus.DRAFT);

        FinalEvaluation saved = evaluationRepository.save(evaluation);
        return mapToResponse(saved, internId -> buildSkillSummaries(internId, new FinalEvaluationResponse()));
    }

    // ─── SUBMIT ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public FinalEvaluationResponse submitEvaluation(Long evaluationId, String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found: " + mentorEmail));

        FinalEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("FinalEvaluation", "id", evaluationId));

        // Chỉ mentor phụ trách mới được gửi
        if (!evaluation.getMentor().getId().equals(mentor.getId())) {
            throw new SecurityException("Bạn không có quyền gửi phê duyệt đánh giá này.");
        }

        if (evaluation.getStatus() == FinalEvaluation.EvaluationStatus.SUBMITTED) {
            throw new IllegalStateException("Đánh giá này đã được gửi trước đó.");
        }

        if (evaluation.getOverallComment() == null || evaluation.getOverallComment().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập nhận xét tổng kết trước khi gửi.");
        }

        // Đánh dấu submitted + locked
        evaluation.setStatus(FinalEvaluation.EvaluationStatus.SUBMITTED);
        evaluation.setIsLocked(true);
        evaluation.setSubmittedAt(LocalDateTime.now());

        FinalEvaluation saved = evaluationRepository.save(evaluation);

        // Khóa tất cả micro_tasks của intern này (không cho submit/review thêm)
        lockInternTasks(evaluation.getIntern().getId());

        FinalEvaluationResponse response = mapToResponseFull(saved);
        return response;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    /**
     * Khóa tất cả task của intern bằng cách đánh dấu các task
     * chưa hoàn tất về trạng thái cuối (REVIEWED).
     * Task ở trạng thái Todo/In_Progress/Submitted sẽ không bị thay đổi,
     * nhưng hệ thống sẽ từ chối mọi thao tác submit/review mới thông qua
     * kiểm tra is_locked trong FinalEvaluation.
     *
     * Cách làm này không thay đổi dữ liệu task cũ, chỉ sử dụng cờ is_locked
     * trên final_evaluation để gate các thao tác.
     */
    private void lockInternTasks(Long internId) {
        // is_locked đã được set = true trên FinalEvaluation.
        // MicroTaskService sẽ kiểm tra cờ này trước khi cho phép submit/review.
        // Không cần thay đổi bảng micro_tasks.
    }

    private void buildSkillSummaries(Long internId, FinalEvaluationResponse response) {
        List<MicroTask> tasks = microTaskRepository.findByInternId(internId);

        int totalAll = tasks.size();
        int totalReviewed = (int) tasks.stream()
                .filter(t -> t.getStatus() == MicroTask.MicroTaskStatus.Reviewed)
                .count();

        response.setTotalTasksAll(totalAll);
        response.setTotalTasksReviewed(totalReviewed);

        // Tổng hợp điểm theo skill từ các task đã REVIEWED
        Map<Long, SkillAggregator> aggregatorMap = new LinkedHashMap<>();

        for (MicroTask task : tasks) {
            if (task.getStatus() != MicroTask.MicroTaskStatus.Reviewed) continue;

            List<TaskSkillRating> ratings = ratingRepository.findByMicroTaskId(task.getId());
            for (TaskSkillRating rating : ratings) {
                if (rating.getRatingScore() == null) continue;

                Long skillId = rating.getSkill().getId();
                aggregatorMap.computeIfAbsent(skillId, k -> new SkillAggregator(
                        rating.getSkill().getId(),
                        rating.getSkill().getName()
                )).add(rating.getRatingScore(), rating.getWeight() != null ? rating.getWeight() : 1);
            }
        }

        List<FinalEvaluationResponse.SkillSummary> summaries = aggregatorMap.values().stream()
                .map(SkillAggregator::toSummary)
                .collect(Collectors.toList());

        response.setSkillSummaries(summaries);
    }

    private FinalEvaluationResponse mapToResponseFull(FinalEvaluation eval) {
        FinalEvaluationResponse response = new FinalEvaluationResponse();
        response.setId(eval.getId());
        response.setInternId(eval.getIntern().getId());
        response.setInternName(eval.getIntern().getName());
        response.setInternEmail(eval.getIntern().getEmail());
        response.setMentorId(eval.getMentor().getId());
        response.setMentorName(eval.getMentor().getName());
        response.setOverallComment(eval.getOverallComment());
        response.setStatus(eval.getStatus().name());
        response.setIsLocked(eval.getIsLocked());
        response.setSubmittedAt(eval.getSubmittedAt());
        response.setCreatedAt(eval.getCreatedAt());
        buildSkillSummaries(eval.getIntern().getId(), response);
        return response;
    }

    private FinalEvaluationResponse mapToResponse(FinalEvaluation eval,
            java.util.function.Function<Long, FinalEvaluationResponse> unused) {
        return mapToResponseFull(eval);
    }

    // Inner helper for aggregating weighted scores per skill
    private static class SkillAggregator {
        private final Long skillId;
        private final String skillName;
        private BigDecimal weightedScoreSum = BigDecimal.ZERO;
        private int totalWeight = 0;
        private int taskCount = 0;

        SkillAggregator(Long skillId, String skillName) {
            this.skillId = skillId;
            this.skillName = skillName;
        }

        void add(BigDecimal score, int weight) {
            weightedScoreSum = weightedScoreSum.add(score.multiply(BigDecimal.valueOf(weight)));
            totalWeight += weight;
            taskCount++;
        }

        FinalEvaluationResponse.SkillSummary toSummary() {
            FinalEvaluationResponse.SkillSummary s = new FinalEvaluationResponse.SkillSummary();
            s.setSkillId(skillId);
            s.setSkillName(skillName);
            s.setTotalWeight(totalWeight);
            s.setTaskCount(taskCount);
            if (totalWeight > 0) {
                s.setAverageScore(weightedScoreSum.divide(
                        BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP));
            } else {
                s.setAverageScore(BigDecimal.ZERO);
            }
            return s;
        }
    }
}
