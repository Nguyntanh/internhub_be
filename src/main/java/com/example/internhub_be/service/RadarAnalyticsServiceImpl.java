package com.example.internhub_be.service;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.response.RadarAnalyticsResponse;
import com.example.internhub_be.repository.InternshipProfileRepository;
import com.example.internhub_be.repository.MicroTaskRepository;
import com.example.internhub_be.repository.TaskSkillRatingRepository;
import com.example.internhub_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RadarAnalyticsServiceImpl implements RadarAnalyticsService {

    /** Điểm benchmark mặc định — mở rộng sau bằng bảng position_skill_benchmarks */
    private static final BigDecimal DEFAULT_BENCHMARK = new BigDecimal("7.0");

    private final UserRepository              userRepository;
    private final MicroTaskRepository         microTaskRepository;
    private final TaskSkillRatingRepository   ratingRepository;
    private final InternshipProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public RadarAnalyticsResponse getRadarByIntern(Long internId, String requesterEmail) {

        // 1. Load intern
        User intern = userRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", internId));

                String internRole = intern.getRole() != null ? intern.getRole().getName() : "";
if (!"INTERN".equals(internRole)) {
    throw new ResourceNotFoundException("Intern", "id", internId);
}

        // 2. Load requester & kiểm tra quyền
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requesterEmail));
        validateAccess(requester, intern);

        // 3. Thông tin vị trí / phòng ban từ InternshipProfile
        String positionName   = null;
        String departmentName = null;

        var profileOpt = profileRepository.findByUserId(internId);
        if (profileOpt.isPresent()) {
            var profile = profileOpt.get();
            if (profile.getPosition() != null) {
                positionName = profile.getPosition().getName();
                if (profile.getPosition().getDepartment() != null) {
                    departmentName = profile.getPosition().getDepartment().getName();
                }
            }
        }

        // 4. Tổng hợp tất cả task của intern
        List<MicroTask> allTasks = microTaskRepository.findByInternId(internId);
        int totalAll      = allTasks.size();
        int totalReviewed = (int) allTasks.stream()
                .filter(t -> t.getStatus() == MicroTask.MicroTaskStatus.Reviewed)
                .count();

        // 5. Tổng hợp điểm kỹ năng — chỉ từ task đã Reviewed
        // key: skillId → SkillAggregator
        Map<Long, SkillAggregator> aggregatorMap = new LinkedHashMap<>();

        for (MicroTask task : allTasks) {
            if (task.getStatus() != MicroTask.MicroTaskStatus.Reviewed) continue;

            List<TaskSkillRating> ratings = ratingRepository.findByMicroTaskId(task.getId());
            for (TaskSkillRating rating : ratings) {
                if (rating.getRatingScore() == null) continue;

                Skill skill = rating.getSkill();
                aggregatorMap
                        .computeIfAbsent(skill.getId(), k -> new SkillAggregator(skill))
                        .add(rating.getRatingScore(),
                             rating.getWeight() != null ? rating.getWeight() : 1);
            }
        }

        // 6. Build SkillScore list
        List<RadarAnalyticsResponse.SkillScore> skillScores = aggregatorMap.values()
                .stream()
                .map(SkillAggregator::toSkillScore)
                .collect(Collectors.toList());

        // 7. Overall score (trung bình có trọng số toàn bộ)
        int        totalW   = aggregatorMap.values().stream().mapToInt(a -> a.totalWeight).sum();
        BigDecimal sumWS    = aggregatorMap.values().stream()
                .map(a -> a.weightedScoreSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallScore = totalW > 0
                ? sumWS.divide(BigDecimal.valueOf(totalW), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 8. Benchmark — mặc định 7.0 cho mỗi skill đã được đánh giá
        List<RadarAnalyticsResponse.BenchmarkScore> benchmarks = aggregatorMap.values().stream()
                .map(agg -> RadarAnalyticsResponse.BenchmarkScore.builder()
                        .skillId(agg.skillId)
                        .skillName(agg.skillName)
                        .benchmarkScore(DEFAULT_BENCHMARK)
                        .build())
                .collect(Collectors.toList());

        return RadarAnalyticsResponse.builder()
                .internId(internId)
                .internName(intern.getName())
                .internEmail(intern.getEmail())
                .positionName(positionName)
                .departmentName(departmentName)
                .skillScores(skillScores)
                .benchmarkScores(benchmarks)
                .totalTasksAll(totalAll)
                .totalTasksReviewed(totalReviewed)
                .overallScore(overallScore)
                .build();
    }

    // ─── Access Control ───────────────────────────────────────────────────────

    /**
     * Kiểm tra quyền theo role:
     *   ADMIN / HR  → Full
     *   MANAGER     → chỉ intern cùng phòng ban
     *   MENTOR      → chỉ intern mình đang phụ trách (có task được giao)
     *   INTERN      → chỉ bản thân
     */
    private void validateAccess(User requester, User intern) {
        String role = requester.getRole() != null ? requester.getRole().getName() : "";

        switch (role) {
            case "ADMIN":
            case "HR":
                break; // không hạn chế

            case "MANAGER":
                boolean sameDept = requester.getDepartment() != null
                        && intern.getDepartment() != null
                        && requester.getDepartment().getId()
                                .equals(intern.getDepartment().getId());
                if (!sameDept) {
                    throw new AccessDeniedException(
                            "Manager chỉ xem được intern trong phòng ban của mình.");
                }
                break;

            case "MENTOR":
                boolean hasTasks = microTaskRepository.findByInternId(intern.getId())
                        .stream()
                        .anyMatch(t -> t.getMentor() != null
                                && t.getMentor().getId().equals(requester.getId()));
                if (!hasTasks) {
                    throw new AccessDeniedException(
                            "Mentor chỉ xem được radar của intern mình phụ trách.");
                }
                break;

            case "INTERN":
                if (!requester.getId().equals(intern.getId())) {
                    throw new AccessDeniedException("Intern chỉ xem được radar của chính mình.");
                }
                break;

            default:
                throw new AccessDeniedException("Không có quyền truy cập.");
        }
    }

    // ─── Inner helper ─────────────────────────────────────────────────────────

    private static class SkillAggregator {
        final Long       skillId;
        final String     skillName;
        final Long       parentSkillId;
        final String     parentSkillName;
        BigDecimal weightedScoreSum = BigDecimal.ZERO;
        int        totalWeight      = 0;
        int        taskCount        = 0;

        SkillAggregator(Skill skill) {
            this.skillId         = skill.getId();
            this.skillName       = skill.getName();
            this.parentSkillId   = skill.getParent() != null ? skill.getParent().getId()   : null;
            this.parentSkillName = skill.getParent() != null ? skill.getParent().getName() : null;
        }

        void add(BigDecimal score, int weight) {
            weightedScoreSum = weightedScoreSum.add(score.multiply(BigDecimal.valueOf(weight)));
            totalWeight += weight;
            taskCount++;
        }

        RadarAnalyticsResponse.SkillScore toSkillScore() {
            BigDecimal avg = totalWeight > 0
                    ? weightedScoreSum.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            return RadarAnalyticsResponse.SkillScore.builder()
                    .skillId(skillId)
                    .skillName(skillName)
                    .parentSkillId(parentSkillId)
                    .parentSkillName(parentSkillName)
                    .score(avg)
                    .totalWeight(totalWeight)
                    .taskCount(taskCount)
                    .build();
        }
    }
}