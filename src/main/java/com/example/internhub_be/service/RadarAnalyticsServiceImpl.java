package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
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

    private static final BigDecimal DEFAULT_BENCHMARK = new BigDecimal("7.0");

    private final UserRepository              userRepository;
    private final MicroTaskRepository         microTaskRepository;
    private final TaskSkillRatingRepository   ratingRepository;
    private final InternshipProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public RadarAnalyticsResponse getRadarByIntern(Long internId, String requesterEmail) {

        User intern = userRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", internId));

        String internRole = intern.getRole() != null ? intern.getRole().getName() : "";
        if (!"INTERN".equals(internRole)) {
            throw new ResourceNotFoundException("Intern", "id", internId);
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", requesterEmail));
        validateAccess(requester, intern);

        return buildRadarResponse(intern);
    }

    @Override
    @Transactional(readOnly = true)
    public RadarAnalyticsResponse getRadarForExport(Long internId) {
        User intern = userRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", internId));
        return buildRadarResponse(intern);
    }

    private RadarAnalyticsResponse buildRadarResponse(User intern) {
        Long internId = intern.getId();

        Optional<InternshipProfile> profileOpt = profileRepository.findByUserId(internId);

        String universityName = null;
        String major          = null;
        String positionName   = null;
        String departmentName = null;
        String status         = null;
        var    startDate      = (java.time.LocalDate) null;
        var    endDate        = (java.time.LocalDate) null;
        Long   mentorId       = null;
        String mentorName     = null;
        Long   managerId      = null;
        String managerName    = null;

        if (profileOpt.isPresent()) {
            InternshipProfile p = profileOpt.get();

            if (p.getUniversity() != null) {
                universityName = p.getUniversity().getName();
            }
            major = p.getMajor();

            if (p.getPosition() != null) {
                positionName = p.getPosition().getName();
                if (p.getPosition().getDepartment() != null) {
                    departmentName = p.getPosition().getDepartment().getName();
                }
            }

            status    = p.getStatus() != null ? p.getStatus().name() : null;
            startDate = p.getStartDate();
            endDate   = p.getEndDate();

            if (p.getMentor() != null) {
                mentorId   = p.getMentor().getId();
                mentorName = p.getMentor().getName();
            }
            if (p.getManager() != null) {
                managerId   = p.getManager().getId();
                managerName = p.getManager().getName();
            }
        }

        List<MicroTask> allTasks = microTaskRepository.findByInternId(internId);
        int totalAll      = allTasks.size();
        int totalReviewed = (int) allTasks.stream()
                .filter(t -> t.getStatus() == MicroTask.MicroTaskStatus.Reviewed)
                .count();

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

        List<RadarAnalyticsResponse.SkillScore> skillScores = aggregatorMap.values()
                .stream()
                .map(SkillAggregator::toSkillScore)
                .collect(Collectors.toList());

        int        totalW = aggregatorMap.values().stream().mapToInt(a -> a.totalWeight).sum();
        BigDecimal sumWS  = aggregatorMap.values().stream()
                .map(a -> a.weightedScoreSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallScore = totalW > 0
                ? sumWS.divide(BigDecimal.valueOf(totalW), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

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
                .phone(intern.getPhone())
                .universityName(universityName)
                .major(major)
                .positionName(positionName)
                .departmentName(departmentName)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .mentorId(mentorId)
                .mentorName(mentorName)
                .managerId(managerId)
                .managerName(managerName)
                .skillScores(skillScores)
                .benchmarkScores(benchmarks)
                .totalTasksAll(totalAll)
                .totalTasksReviewed(totalReviewed)
                .overallScore(overallScore)
                .build();
    }

    private void validateAccess(User requester, User intern) {
        String role = requester.getRole() != null ? requester.getRole().getName() : "";

        switch (role) {
            case "ADMIN", "HR" -> {}

            case "MANAGER" -> {
                boolean sameDept = requester.getDepartment() != null
                        && intern.getDepartment() != null
                        && requester.getDepartment().getId()
                        .equals(intern.getDepartment().getId());
                if (!sameDept) {
                    throw new AccessDeniedException(
                            "Manager chỉ xem được intern trong phòng ban của mình.");
                }
            }

            case "MENTOR" -> {
                boolean hasTasks = microTaskRepository.findByInternId(intern.getId())
                        .stream()
                        .anyMatch(t -> t.getMentor() != null
                                && t.getMentor().getId().equals(requester.getId()));
                if (!hasTasks) {
                    throw new AccessDeniedException(
                            "Mentor chỉ xem được radar của intern mình phụ trách.");
                }
            }

            case "INTERN" -> {
                if (!requester.getId().equals(intern.getId())) {
                    throw new AccessDeniedException("Intern chỉ xem được radar của chính mình.");
                }
            }

            default -> throw new AccessDeniedException("Không có quyền truy cập.");
        }
    }

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