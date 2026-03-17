package com.example.internhub_be.service;

import com.example.internhub_be.domain.*;
import com.example.internhub_be.payload.request.*;
import com.example.internhub_be.payload.response.*;
import com.example.internhub_be.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MicroTaskServiceImpl implements MicroTaskService {

    private final MicroTaskRepository microTaskRepository;
    private final TaskSkillRatingRepository taskSkillRatingRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    /*
    =========================
    CREATE TASK
    =========================
    */

    @Override
    public void createTask(CreateMicroTaskRequest request) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        User mentor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        for (Long internId : request.getInternIds()) {

            User intern = userRepository.findById(internId)
                    .orElseThrow(() -> new RuntimeException("Intern not found"));

            MicroTask task = new MicroTask();

            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDeadline(request.getDeadline());
            task.setMentor(mentor);
            task.setIntern(intern);

            task = microTaskRepository.saveAndFlush(task);

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

                taskSkillRatingRepository.save(rating);
            }
        }
    }

    /*
    =========================
    TASKS FOR INTERN
    =========================
    */

    @Override
    public List<TaskResponse> getTasksForCurrentIntern() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User intern = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<MicroTask> tasks = microTaskRepository.findByIntern(intern);

        return tasks.stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus().name())
                        .deadline(task.getDeadline())
                        .build())
                .collect(Collectors.toList());
    }

    /*
    =========================
    TASKS FOR MENTOR
    =========================
    */

    @Override
    public List<TaskResponse> getTasksForCurrentMentor() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User mentor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<MicroTask> tasks = microTaskRepository.findByMentor(mentor);

        return tasks.stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus().name())
                        .deadline(task.getDeadline())
                        .build())
                .collect(Collectors.toList());
    }

    /*
    =========================
    TASK DETAIL
    =========================
    */

    @Override
    public TaskDetailResponse getTaskDetail(Long taskId) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        List<TaskSkillRating> ratings =
                taskSkillRatingRepository.findByMicroTask(task);

        TaskDetailResponse response = new TaskDetailResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setDeadline(task.getDeadline());
        response.setStatus(task.getStatus().name());
        response.setSubmissionLink(task.getSubmissionLink());
        response.setSubmissionNote(task.getSubmissionNote());

    /*
    =========================
    INTERN
    =========================
    */

        List<InternResponse> internResponses = new ArrayList<>();

        if (task.getIntern() != null) {

            InternResponse intern = new InternResponse();

            intern.setId(task.getIntern().getId());
            intern.setName(task.getIntern().getName());
            intern.setEmail(task.getIntern().getEmail());

            internResponses.add(intern);
        }

        response.setAssignedInterns(internResponses);

    /*
    =========================
    SKILLS
    =========================
    */

        List<SkillRatingResponse> skillResponses = new ArrayList<>();

        for (TaskSkillRating r : ratings) {

            SkillRatingResponse sr = new SkillRatingResponse();

            sr.setSkillId(r.getSkill().getId());
            sr.setSkillName(r.getSkill().getName());
            sr.setWeight(r.getWeight());
            sr.setRatingScore(r.getRatingScore());
            sr.setReviewComment(r.getReviewComment());

            skillResponses.add(sr);
        }

        response.setSkills(skillResponses);

        return response;
    }

    /*
    =========================
    SUBMIT TASK
    =========================
    */

    @Override
    public void submitTask(Long taskId, SubmitTaskRequest request) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setSubmissionLink(request.getSubmissionLink());
        task.setSubmissionNote(request.getSubmissionNote());
        task.setStatus(MicroTask.MicroTaskStatus.Submitted);

        microTaskRepository.save(task);
    }

    /*
    =========================
    DELETE TASK
    =========================
    */

    @Override
    public void deleteTask(Long taskId) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        List<TaskSkillRating> ratings =
                taskSkillRatingRepository.findByMicroTask(task);

        taskSkillRatingRepository.deleteAll(ratings);

        microTaskRepository.delete(task);
    }

    /*
    =========================
    REVIEW TASK
    =========================
    */

    @Override
    public void reviewTask(Long taskId, ReviewTaskRequest request) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        List<TaskSkillRating> ratings =
                taskSkillRatingRepository.findByMicroTask(task);

        for (ReviewSkillRequest skillReq : request.getSkills()) {

            for (TaskSkillRating rating : ratings) {

                if (rating.getSkill().getId().equals(skillReq.getSkillId())) {

                    rating.setRatingScore(skillReq.getRatingScore());
                    rating.setReviewComment(skillReq.getReviewComment());

                    taskSkillRatingRepository.save(rating);
                }
            }
        }

        task.setStatus(MicroTask.MicroTaskStatus.Reviewed);

        microTaskRepository.save(task);
    }
    /*
=========================
UPDATE TASK
=========================
*/

    @Override
    public void updateTask(Long taskId, UpdateMicroTaskRequest request) {

        MicroTask task = microTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());

        microTaskRepository.save(task);
    }



}

