package com.example.internhub_be.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.internhub_be.domain.InternshipPosition;
import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.TaskSkillRatingId;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.request.TaskAssignmentRequest;
import com.example.internhub_be.repository.MicroTaskRepository;
import com.example.internhub_be.repository.SkillRepository;
import com.example.internhub_be.repository.TaskSkillRatingRepository;
import com.example.internhub_be.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final MicroTaskRepository taskRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TaskSkillRatingRepository ratingRepository;

    @Override
    @Transactional
    public MicroTask createAndAssignTask(TaskAssignmentRequest request, User mentor) {
        // 1. Tìm Intern và kiểm tra tồn tại
        User intern = userRepository.findById(request.getInternId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getInternId()));

        // 2. Khởi tạo MicroTask
        MicroTask task = new MicroTask();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setMentor(mentor);
        task.setIntern(intern);
        task.setStatus(MicroTask.MicroTaskStatus.Todo);

        MicroTask savedTask = taskRepository.save(task);

        // 3. Lưu danh sách Skill Tags & Trọng số
        if (request.getSkills() == null || request.getSkills().isEmpty()) {
            throw new IllegalArgumentException("At least one skill tag is required");
        }

        for (var sReq : request.getSkills()) {
            Skill skill = skillRepository.findById(sReq.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", sReq.getSkillId()));

            TaskSkillRating rating = new TaskSkillRating();
            // Thiết lập ID phức hợp
            rating.setId(new TaskSkillRatingId(savedTask.getId(), skill.getId()));
            rating.setMicroTask(savedTask);
            rating.setSkill(skill);
            rating.setWeight(sReq.getWeight());
            
            ratingRepository.save(rating);
        }

        return savedTask;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> getSuggestedSkills(Long internId) {
        // Sửa lỗi "Cannot infer type argument": Ép kiểu tường minh trong chuỗi map
        return userRepository.findById(internId)
            .map(User::getInternshipProfile)
            .map(InternshipProfile::getPosition)
            .map((InternshipPosition pos) -> skillRepository.findByPosition(pos)) 
            .orElse(Collections.emptyList());
    }
}