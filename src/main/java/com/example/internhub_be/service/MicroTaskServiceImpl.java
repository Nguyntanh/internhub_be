package com.example.internhub_be.service;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.TaskSkillRatingId;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.request.CreateMicroTaskRequest;
import com.example.internhub_be.payload.request.ReviewTaskRequest;
import com.example.internhub_be.payload.request.SkillWeightRequest;
import com.example.internhub_be.payload.request.SubmitTaskRequest;
import com.example.internhub_be.repository.MicroTaskRepository;
import com.example.internhub_be.repository.SkillRepository;
import com.example.internhub_be.repository.TaskSkillRatingRepository;
import com.example.internhub_be.repository.UserRepository;
import com.example.internhub_be.service.MicroTaskService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.internhub_be.payload.response.TaskDetailResponse;
import com.example.internhub_be.payload.response.TaskSkillResponse;
import java.util.stream.Collectors;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MicroTaskServiceImpl implements MicroTaskService {

    private final MicroTaskRepository microTaskRepository;
    private final SkillRepository skillRepository;
    private final TaskSkillRatingRepository ratingRepository;
    private final UserRepository userRepository;

    // ================= CREATE TASK =================

    @Override
    @Transactional
    public void createTask(CreateMicroTaskRequest request, Long mentorId) {

        if (request.getSkills() == null || request.getSkills().isEmpty()) {
            throw new RuntimeException("At least one skill must be selected");
        }

        if (request.getInternIds() == null || request.getInternIds().isEmpty()) {
            throw new RuntimeException("At least one intern must be assigned");
        }

        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        for (Long internId : request.getInternIds()) {

            User intern = userRepository.findById(internId)
                    .orElseThrow(() -> new RuntimeException("Intern not found"));

            MicroTask task = new MicroTask();

            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDeadline(request.getDeadline());
            task.setStatus(MicroTask.MicroTaskStatus.Todo);
            task.setMentor(mentor);
            task.setIntern(intern);

            microTaskRepository.save(task);

            for (SkillWeightRequest skillReq : request.getSkills()) {

                Skill skill = skillRepository.findById(skillReq.getSkillId())
                        .orElseThrow(() -> new RuntimeException("Skill not found"));

                TaskSkillRating rating = new TaskSkillRating();

                TaskSkillRatingId id = new TaskSkillRatingId(
                        task.getId(),
                        skill.getId()
                );

                rating.setId(id);
                rating.setMicroTask(task);
                rating.setSkill(skill);
                rating.setWeight(skillReq.getWeight());

                ratingRepository.save(rating);
            }
        }
    }

    // ================= GET TASKS BY INTERN =================

    @Override
    public List<MicroTask> getTasksByIntern(Long internId) {

        return microTaskRepository.findByInternId(internId);

    }

    // ================= SUBMIT TASK =================

    @Override
    @Transactional
    public void submitTask(Long taskId, SubmitTaskRequest request) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // chỉ cho submit khi task đang Todo hoặc In_Progress
        if(task.getStatus() != MicroTask.MicroTaskStatus.Todo &&
                task.getStatus() != MicroTask.MicroTaskStatus.In_Progress){
            throw new RuntimeException("Task cannot be submitted");
        }

        task.setSubmissionLink(request.getSubmissionLink());
        task.setSubmissionNote(request.getSubmissionNote());

        task.setStatus(MicroTask.MicroTaskStatus.Submitted);

        microTaskRepository.save(task);
    }

    @Override
    @Transactional
    public void reviewTask(Long taskId, ReviewTaskRequest request) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // chỉ review khi task đã submit
        if(task.getStatus() != MicroTask.MicroTaskStatus.Submitted){
            throw new RuntimeException("Task must be submitted before review");
        }

        request.getSkills().forEach(skillReq -> {

            TaskSkillRatingId id =
                    new TaskSkillRatingId(taskId, skillReq.getSkillId());

            TaskSkillRating rating = ratingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Skill rating not found"));

            rating.setRatingScore(skillReq.getScore());
            rating.setReviewComment(skillReq.getComment());

            ratingRepository.save(rating);
        });

        task.setStatus(MicroTask.MicroTaskStatus.Reviewed);

        microTaskRepository.save(task);
    }
    @Override
    public TaskDetailResponse getTaskDetail(Long taskId) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        List<TaskSkillRating> ratings =
                ratingRepository.findByMicroTaskId(taskId);

        List<TaskSkillResponse> skills =
                ratings.stream().map(rating -> {

                    TaskSkillResponse res = new TaskSkillResponse();

                    res.setSkillId(rating.getSkill().getId());
                    res.setSkillName(rating.getSkill().getName());
                    res.setWeight(rating.getWeight());
                    res.setScore(rating.getRatingScore());
                    res.setComment(rating.getReviewComment());

                    return res;

                }).collect(Collectors.toList());

        TaskDetailResponse response = new TaskDetailResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus().name());
        response.setSkills(skills);

        return response;
    }

}