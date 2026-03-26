package com.example.internhub_be.service;

import com.example.internhub_be.domain.FinalEvaluation;
import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.Notification;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.payload.request.FinalEvaluationRequest;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;
import com.example.internhub_be.repository.FinalEvaluationRepository;
import com.example.internhub_be.repository.InternshipProfileRepository;
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
    private final InternshipProfileRepository internshipProfileRepository;
    private final MicroTaskRepository microTaskRepository;
    private final TaskSkillRatingRepository ratingRepository;
    // ── THÊM MỚI: inject NotificationService để gửi thông báo cho Manager ──
    private final NotificationService notificationService;

    // ─── GET MY INTERNS ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getMyInterns(String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found: " + mentorEmail));

        return internshipProfileRepository.findByMentorId(mentor.getId())
                .stream()
                .map(profile -> mapUserToUserResponse(profile.getUser()))
                .collect(Collectors.toList());
    }

    // ─── GET EVALUATION ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public FinalEvaluationResponse getEvaluationByIntern(Long internId) {
        userRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", internId));

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

        buildSkillSummaries(internId, response);
        return response;
    }

    // ─── SAVE / UPDATE DRAFT ──────────────────────────────────────────────────

    @Override
    @Transactional
    public FinalEvaluationResponse saveOrUpdateDraft(FinalEvaluationRequest request, String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found: " + mentorEmail));

        User intern = userRepository.findById(request.getInternId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getInternId()));

        FinalEvaluation evaluation = evaluationRepository.findByInternId(request.getInternId())
                .orElse(new FinalEvaluation());

        if (evaluation.getId() != null && Boolean.TRUE.equals(evaluation.getIsLocked())) {
            throw new IllegalStateException("Đánh giá đang bị khóa. Hãy dùng 'Đánh giá lại' để mở khóa trước.");
        }

        evaluation.setIntern(intern);
        evaluation.setMentor(mentor);
        evaluation.setOverallComment(request.getOverallComment());
        evaluation.setStatus(FinalEvaluation.EvaluationStatus.DRAFT);
        if (evaluation.getId() == null) {
            evaluation.setIsLocked(false);
        }

        FinalEvaluation saved = evaluationRepository.save(evaluation);
        return mapToResponseFull(saved);
    }

    // ─── SUBMIT ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public FinalEvaluationResponse submitEvaluation(Long evaluationId, String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found: " + mentorEmail));

        FinalEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("FinalEvaluation", "id", evaluationId));

        if (!evaluation.getMentor().getId().equals(mentor.getId())) {
            throw new SecurityException("Bạn không có quyền gửi phê duyệt đánh giá này.");
        }

        if (Boolean.TRUE.equals(evaluation.getIsLocked())) {
            throw new IllegalStateException("Đánh giá này đã được khóa. Dùng 'Đánh giá lại' để mở khóa trước.");
        }

        if (evaluation.getOverallComment() == null || evaluation.getOverallComment().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập nhận xét tổng kết trước khi gửi.");
        }

        evaluation.setStatus(FinalEvaluation.EvaluationStatus.SUBMITTED);
        evaluation.setIsLocked(true);
        evaluation.setSubmittedAt(LocalDateTime.now());

        FinalEvaluation saved = evaluationRepository.save(evaluation);

        // ── THÊM MỚI: Gửi thông báo tới tất cả Manager sau khi submit ────────
        notifyManagers(saved, mentor);

        return mapToResponseFull(saved);
    }

    // ─── RESET (Đánh giá lại) ─────────────────────────────────────────────────

    @Override
    @Transactional
    public FinalEvaluationResponse resetEvaluation(Long evaluationId, String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found: " + mentorEmail));

        FinalEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("FinalEvaluation", "id", evaluationId));

        if (!evaluation.getMentor().getId().equals(mentor.getId())) {
            throw new SecurityException("Bạn không có quyền mở khóa đánh giá này.");
        }

        evaluation.setStatus(FinalEvaluation.EvaluationStatus.DRAFT);
        evaluation.setIsLocked(false);
        evaluation.setSubmittedAt(null);

        FinalEvaluation saved = evaluationRepository.save(evaluation);
        return mapToResponseFull(saved);
    }

    // ─── THÊM MỚI: Gửi thông báo tới tất cả Manager ─────────────────────────

    private void notifyManagers(FinalEvaluation evaluation, User mentor) {
        List<User> managers = userRepository.findByRole_Name("MANAGER");
        if (managers.isEmpty()) return;

        User intern = evaluation.getIntern();
        String title = "Có đánh giá mới cần xem xét";
        String message = String.format(
                "Mentor %s vừa gửi đánh giá cuối kỳ cho intern %s. Vui lòng xem xét và ra quyết định.",
                mentor.getName(), intern.getName());

        for (User manager : managers) {
            notificationService.createNotification(
                    manager,
                    mentor,
                    Notification.NotificationType.EVALUATION_SUBMITTED,
                    title,
                    message,
                    evaluation.getId(),
                    "FinalEvaluation"
            );
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private void buildSkillSummaries(Long internId, FinalEvaluationResponse response) {
        List<MicroTask> tasks = microTaskRepository.findByInternId(internId);

        int totalAll = tasks.size();
        int totalReviewed = (int) tasks.stream()
                .filter(t -> t.getStatus() == MicroTask.MicroTaskStatus.Reviewed)
                .count();

        response.setTotalTasksAll(totalAll);
        response.setTotalTasksReviewed(totalReviewed);

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

    private UserResponse mapUserToUserResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setIsActive(user.getIsActive());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());
        resp.setCreatedAt(user.getCreatedAt());
        if (user.getRole() != null) {
            resp.setRoleId(user.getRole().getId());
            resp.setRoleName(user.getRole().getName());
        }
        if (user.getDepartment() != null) {
            resp.setDepartmentId(user.getDepartment().getId());
            resp.setDepartmentName(user.getDepartment().getName());
        }
        return resp;
    }

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
            s.setAverageScore(totalWeight > 0
                    ? weightedScoreSum.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            return s;
        }
    }
}