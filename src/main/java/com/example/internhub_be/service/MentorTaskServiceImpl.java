package com.example.internhub_be.service;

import com.example.internhub_be.domain.*;
import com.example.internhub_be.domain.MicroTask.MicroTaskStatus;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.*;
import com.example.internhub_be.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorTaskServiceImpl implements MentorTaskService {

    private final MicroTaskRepository microTaskRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final TaskSkillRatingRepository taskSkillRatingRepository;
    private final InternTaskServiceImpl internTaskService;

    public MentorTaskServiceImpl(MicroTaskRepository microTaskRepository,
                                 UserRepository userRepository,
                                 SkillRepository skillRepository,
                                 TaskSkillRatingRepository taskSkillRatingRepository,
                                 InternTaskServiceImpl internTaskService) {
        this.microTaskRepository = microTaskRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.taskSkillRatingRepository = taskSkillRatingRepository;
        this.internTaskService = internTaskService;
    }

    @Override
    public List<MicroTaskResponse> getTasksAssignedByMe(String mentorEmail) {
        User mentor = getUserByEmail(mentorEmail);
        return microTaskRepository.findByMentor(mentor)
                .stream().map(internTaskService::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MicroTaskResponse> getSubmittedTasks(String mentorEmail) {
        User mentor = getUserByEmail(mentorEmail);
        return microTaskRepository.findByMentorAndStatus(mentor, MicroTaskStatus.Submitted)
                .stream().map(internTaskService::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MicroTaskResponse reviewTask(Long taskId, TaskReviewRequest request, String mentorEmail) {
        User mentor = getUserByEmail(mentorEmail);
        MicroTask task = getTaskById(taskId);
        validateMentorOwnership(task, mentor);

        if (task.getStatus() != MicroTaskStatus.Submitted) {
            throw new IllegalStateException(
                "Chỉ có thể đánh giá task ở trạng thái Submitted. Hiện tại: " + task.getStatus()
            );
        }
        if (request.getStatus() != MicroTaskStatus.Reviewed && request.getStatus() != MicroTaskStatus.Rejected) {
            throw new IllegalArgumentException("Trạng thái chỉ được là REVIEWED hoặc REJECTED.");
        }

        task.setStatus(request.getStatus() == MicroTaskStatus.Rejected
                ? MicroTaskStatus.In_Progress
                : MicroTaskStatus.Reviewed);

        return internTaskService.mapToResponse(microTaskRepository.save(task));
    }

    @Override
    @Transactional
    public List<TaskSkillRatingResponse> rateTaskSkills(Long taskId,
                                                         List<TaskSkillRatingRequest> ratings,
                                                         String mentorEmail) {
        User mentor = getUserByEmail(mentorEmail);
        MicroTask task = getTaskById(taskId);
        validateMentorOwnership(task, mentor);

        if (task.getStatus() != MicroTaskStatus.Submitted && task.getStatus() != MicroTaskStatus.Reviewed) {
            throw new IllegalStateException("Chỉ chấm điểm được task ở trạng thái Submitted hoặc Reviewed.");
        }

        return ratings.stream().map(req -> {
            Skill skill = skillRepository.findById(req.getSkillId().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", req.getSkillId()));
            TaskSkillRatingId ratingId = new TaskSkillRatingId(task.getId(), skill.getId());
            TaskSkillRating rating = taskSkillRatingRepository.findById(ratingId)
                    .orElse(new TaskSkillRating());
            rating.setId(ratingId);
            rating.setMicroTask(task);
            rating.setSkill(skill);
            rating.setWeight(req.getWeight() != null ? req.getWeight() : 1);
            rating.setRatingScore(req.getRatingScore());
            rating.setReviewComment(req.getReviewComment());
            return mapToSkillRatingResponse(taskSkillRatingRepository.save(rating));
        }).collect(Collectors.toList());
    }

    @Override
    public List<TaskSkillRatingResponse> getTaskSkillRatings(Long taskId, String mentorEmail) {
        User mentor = getUserByEmail(mentorEmail);
        MicroTask task = getTaskById(taskId);
        validateMentorOwnership(task, mentor);
        return taskSkillRatingRepository.findByMicroTask(task)
                .stream().map(this::mapToSkillRatingResponse).collect(Collectors.toList());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private MicroTask getTaskById(Long taskId) {
        return microTaskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("MicroTask", "id", taskId));
    }

    private void validateMentorOwnership(MicroTask task, User mentor) {
        if (task.getMentor() == null || !task.getMentor().getId().equals(mentor.getId())) {
            throw new AccessDeniedException("Bạn không có quyền đánh giá task này.");
        }
    }

    private TaskSkillRatingResponse mapToSkillRatingResponse(TaskSkillRating r) {
        TaskSkillRatingResponse res = new TaskSkillRatingResponse();
        res.setTaskId(r.getMicroTask().getId());
        res.setSkillId(r.getSkill().getId());
        res.setSkillName(r.getSkill().getName());
        res.setWeight(r.getWeight());
        res.setRatingScore(r.getRatingScore());
        res.setReviewComment(r.getReviewComment());
        return res;
    }
}