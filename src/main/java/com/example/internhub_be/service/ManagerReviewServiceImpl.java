package com.example.internhub_be.service;

import com.example.internhub_be.domain.*;
import com.example.internhub_be.domain.InternshipDecision.DecisionType;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.request.ManagerDecisionRequest;
import com.example.internhub_be.payload.response.ManagerReviewResponse;
import com.example.internhub_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerReviewServiceImpl implements ManagerReviewService {

    private final InternshipProfileRepository   profileRepository;
    private final FinalEvaluationRepository     evaluationRepository;
    private final InternshipDecisionRepository  decisionRepository;
    private final MicroTaskRepository           microTaskRepository;
    private final TaskSkillRatingRepository     ratingRepository;
    private final UserRepository                userRepository;
    private final NotificationService           notificationService;

    // ─── DANH SÁCH CHỜ DUYỆT ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ManagerReviewResponse> getPendingReviews(String managerEmail, boolean pendingOnly) {
        User manager = getUserByEmail(managerEmail);
        validateManagerRole(manager);

        // Lấy tất cả FinalEvaluation đã SUBMITTED
        List<FinalEvaluation> submitted = evaluationRepository.findAll().stream()
                .filter(e -> e.getStatus() == FinalEvaluation.EvaluationStatus.SUBMITTED)
                .collect(Collectors.toList());

        return submitted.stream()
                .filter(eval -> {
                    if (!pendingOnly) return true;
                    // Chỉ lấy hồ sơ chưa có quyết định
                    Optional<InternshipProfile> profileOpt =
                            profileRepository.findByUserId(eval.getIntern().getId());
                    return profileOpt.isPresent()
                            && !decisionRepository.existsByInternshipProfileId(profileOpt.get().getId());
                })
                .map(eval -> {
                    Optional<InternshipProfile> profileOpt =
                            profileRepository.findByUserId(eval.getIntern().getId());
                    if (profileOpt.isEmpty()) return null;
                    return buildResponse(profileOpt.get(), eval);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ─── CHI TIẾT BÁO CÁO ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ManagerReviewResponse getReviewDetail(Long internshipProfileId, String managerEmail) {
        User manager = getUserByEmail(managerEmail);
        validateManagerRole(manager);

        InternshipProfile profile = profileRepository.findByIdWithRelations(internshipProfileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InternshipProfile", "id", internshipProfileId));

        // FinalEvaluation phải tồn tại và đã SUBMITTED
        FinalEvaluation eval = evaluationRepository
                .findByInternId(profile.getUser().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Intern này chưa có đánh giá của Mentor hoặc chưa được gửi phê duyệt."));

        if (eval.getStatus() != FinalEvaluation.EvaluationStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Đánh giá chưa được Mentor gửi phê duyệt (trạng thái hiện tại: "
                            + eval.getStatus() + ").");
        }

        return buildResponse(profile, eval);
    }

    // ─── MANAGER RA QUYẾT ĐỊNH ────────────────────────────────────────────

    @Override
    @Transactional
    public ManagerReviewResponse submitDecision(ManagerDecisionRequest request, String managerEmail) {
        User manager = getUserByEmail(managerEmail);
        validateManagerRole(manager);

        InternshipProfile profile = profileRepository
                .findByIdWithRelations(request.getInternshipProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InternshipProfile", "id", request.getInternshipProfileId()));

        // Kiểm tra FinalEvaluation đã SUBMITTED
        FinalEvaluation eval = evaluationRepository
                .findByInternId(profile.getUser().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Chưa có đánh giá của Mentor được gửi phê duyệt cho intern này."));

        if (eval.getStatus() != FinalEvaluation.EvaluationStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Đánh giá của Mentor chưa ở trạng thái SUBMITTED.");
        }

        // Tạo hoặc cập nhật quyết định
        InternshipDecision decision = decisionRepository
                .findByInternshipProfileId(profile.getId())
                .orElse(new InternshipDecision());

        decision.setInternshipProfile(profile);
        decision.setManager(manager);
        decision.setFinalEvaluation(eval);
        decision.setDecision(request.getDecision());
        decision.setManagerComment(request.getManagerComment());
        decision.setHrNotified(false);

        InternshipDecision saved = decisionRepository.save(decision);

        // Gửi thông báo tới HR
        notifyHR(saved, profile, manager);

        // Cập nhật InternshipProfile.status nếu cần
        updateInternshipStatus(profile, request.getDecision());

        return buildResponse(profile, eval);
    }

    // ─── BUILD RESPONSE ────────────────────────────────────────────────────

    private ManagerReviewResponse buildResponse(InternshipProfile profile, FinalEvaluation eval) {
        User intern = profile.getUser();

        // Tổng hợp điểm kỹ năng
        SkillAggregationResult skillResult = aggregateSkills(intern.getId());

        // Lịch sử task
        List<ManagerReviewResponse.TaskHistoryItem> taskHistory = buildTaskHistory(intern.getId());

        // Quyết định hiện tại (nếu có)
        ManagerReviewResponse.DecisionInfo decisionInfo = decisionRepository
                .findByInternshipProfileId(profile.getId())
                .map(d -> ManagerReviewResponse.DecisionInfo.builder()
                        .decisionId(d.getId())
                        .decision(d.getDecision())
                        .managerComment(d.getManagerComment())
                        .managerName(d.getManager().getName())
                        .createdAt(d.getCreatedAt())
                        .hrNotified(d.getHrNotified())
                        .build())
                .orElse(null);

        return ManagerReviewResponse.builder()
                // Intern
                .internId(intern.getId())
                .internName(intern.getName())
                .internEmail(intern.getEmail())
                .internPhone(intern.getPhone())
                .internAvatar(intern.getAvatar())
                .universityName(profile.getUniversity() != null ? profile.getUniversity().getName() : null)
                .major(profile.getMajor())
                // Hồ sơ
                .internshipProfileId(profile.getId())
                .positionName(profile.getPosition() != null ? profile.getPosition().getName() : null)
                .departmentName(profile.getPosition() != null && profile.getPosition().getDepartment() != null
                        ? profile.getPosition().getDepartment().getName() : null)
                .startDate(profile.getStartDate())
                .endDate(profile.getEndDate())
                .internshipStatus(profile.getStatus() != null ? profile.getStatus().name() : null)
                // Mentor
                .mentorId(profile.getMentor() != null ? profile.getMentor().getId() : null)
                .mentorName(profile.getMentor() != null ? profile.getMentor().getName() : null)
                .mentorEmail(profile.getMentor() != null ? profile.getMentor().getEmail() : null)
                // Đánh giá Mentor
                .evaluationId(eval.getId())
                .overallComment(eval.getOverallComment())
                .evaluationStatus(eval.getStatus().name())
                .evaluationSubmittedAt(eval.getSubmittedAt())
                // Điểm kỹ năng
                .skillSummaries(skillResult.summaries)
                .overallScore(skillResult.overallScore)
                .totalTasksAll(skillResult.totalAll)
                .totalTasksReviewed(skillResult.totalReviewed)
                // Lịch sử task
                .taskHistory(taskHistory)
                // Quyết định
                .currentDecision(decisionInfo)
                .build();
    }

    // ─── TỔNG HỢP ĐIỂM KỸ NĂNG ────────────────────────────────────────────

    private SkillAggregationResult aggregateSkills(Long internId) {
        List<MicroTask> tasks = microTaskRepository.findByInternId(internId);
        int totalAll = tasks.size();
        int totalReviewed = (int) tasks.stream()
                .filter(t -> t.getStatus() == MicroTask.MicroTaskStatus.Reviewed)
                .count();

        // Nhóm theo skill, tính trung bình có trọng số
        Map<Long, SkillAccumulator> accMap = new LinkedHashMap<>();
        for (MicroTask task : tasks) {
            if (task.getStatus() != MicroTask.MicroTaskStatus.Reviewed) continue;
            for (TaskSkillRating r : ratingRepository.findByMicroTaskId(task.getId())) {
                if (r.getRatingScore() == null) continue;
                accMap.computeIfAbsent(r.getSkill().getId(),
                                k -> new SkillAccumulator(r.getSkill()))
                      .add(r.getRatingScore(), r.getWeight() != null ? r.getWeight() : 1);
            }
        }

        List<ManagerReviewResponse.SkillSummaryItem> summaries = accMap.values().stream()
                .map(acc -> {
                    BigDecimal avg = acc.totalWeight > 0
                            ? acc.weightedSum.divide(BigDecimal.valueOf(acc.totalWeight), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return ManagerReviewResponse.SkillSummaryItem.builder()
                            .skillId(acc.skill.getId())
                            .skillName(acc.skill.getName())
                            .parentSkillName(acc.skill.getParent() != null ? acc.skill.getParent().getName() : null)
                            .averageScore(avg)
                            .totalWeight(acc.totalWeight)
                            .taskCount(acc.taskCount)
                            .build();
                })
                .collect(Collectors.toList());

        int totalW = accMap.values().stream().mapToInt(a -> a.totalWeight).sum();
        BigDecimal sumWS = accMap.values().stream()
                .map(a -> a.weightedSum).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overall = totalW > 0
                ? sumWS.divide(BigDecimal.valueOf(totalW), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        SkillAggregationResult result = new SkillAggregationResult();
        result.summaries     = summaries;
        result.overallScore  = overall;
        result.totalAll      = totalAll;
        result.totalReviewed = totalReviewed;
        return result;
    }

    // ─── LỊCH SỬ TASK ─────────────────────────────────────────────────────

    private List<ManagerReviewResponse.TaskHistoryItem> buildTaskHistory(Long internId) {
        return microTaskRepository.findByInternId(internId).stream()
                .map(task -> {
                    List<ManagerReviewResponse.TaskSkillItem> skills =
                            ratingRepository.findByMicroTaskId(task.getId()).stream()
                                    .map(r -> ManagerReviewResponse.TaskSkillItem.builder()
                                            .skillName(r.getSkill().getName())
                                            .weight(r.getWeight())
                                            .ratingScore(r.getRatingScore())
                                            .reviewComment(r.getReviewComment())
                                            .build())
                                    .collect(Collectors.toList());

                    return ManagerReviewResponse.TaskHistoryItem.builder()
                            .taskId(task.getId())
                            .title(task.getTitle())
                            .description(task.getDescription())
                            .status(task.getStatus().name())
                            .deadline(task.getDeadline())
                            .createdAt(task.getCreatedAt())
                            .skills(skills)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─── THÔNG BÁO HR ─────────────────────────────────────────────────────

    private void notifyHR(InternshipDecision decision,
                          InternshipProfile profile,
                          User manager) {
        // Tìm tất cả user có role HR để gửi thông báo
        List<User> hrUsers = userRepository.findByRole_Name("HR");
        if (hrUsers.isEmpty()) return;

        String internName = profile.getUser().getName();
        String decisionLabel = switch (decision.getDecision()) {
            case PASS             -> "Đạt (Pass)";
            case FAIL             -> "Không đạt (Fail)";
            case CONVERT_TO_STAFF -> "Tuyển dụng chính thức (Convert to Staff)";
        };

        Notification.NotificationType notifType = switch (decision.getDecision()) {
            case PASS             -> Notification.NotificationType.DECISION_PASS;
            case FAIL             -> Notification.NotificationType.DECISION_FAIL;
            case CONVERT_TO_STAFF -> Notification.NotificationType.DECISION_CONVERT_TO_STAFF;
        };

        String title   = "Quyết định thực tập: " + internName;
        String message = String.format(
                "Manager %s đã ra quyết định \"%s\" cho intern %s. " +
                "Vui lòng làm thủ tục cần thiết.",
                manager.getName(), decisionLabel, internName);

        for (User hr : hrUsers) {
            notificationService.createNotification(
                    hr, manager, notifType, title, message,
                    decision.getId(), "InternshipDecision");
        }

        // Đánh dấu đã thông báo HR
        decision.setHrNotified(true);
        decision.setHrNotifiedAt(LocalDateTime.now());
        decisionRepository.save(decision);
    }

    // ─── CẬP NHẬT STATUS HỒ SƠ ────────────────────────────────────────────

    private void updateInternshipStatus(InternshipProfile profile, DecisionType decision) {
        InternshipProfile.InternshipStatus newStatus = switch (decision) {
            case PASS, CONVERT_TO_STAFF -> InternshipProfile.InternshipStatus.Completed;
            case FAIL                   -> InternshipProfile.InternshipStatus.Terminated;
        };
        profile.setStatus(newStatus);
        profileRepository.save(profile);
    }

    // ─── VALIDATE ─────────────────────────────────────────────────────────

    private void validateManagerRole(User user) {
        String role = user.getRole() != null ? user.getRole().getName() : "";
        if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Chỉ Manager hoặc Admin mới có quyền thực hiện chức năng này.");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // ─── Inner helpers ─────────────────────────────────────────────────────

    private static class SkillAccumulator {
        final Skill skill;
        BigDecimal weightedSum = BigDecimal.ZERO;
        int totalWeight = 0;
        int taskCount = 0;

        SkillAccumulator(Skill skill) { this.skill = skill; }

        void add(BigDecimal score, int weight) {
            weightedSum = weightedSum.add(score.multiply(BigDecimal.valueOf(weight)));
            totalWeight += weight;
            taskCount++;
        }
    }

    private static class SkillAggregationResult {
        List<ManagerReviewResponse.SkillSummaryItem> summaries;
        BigDecimal overallScore;
        int totalAll;
        int totalReviewed;
    }
}