package com.example.internhub_be.service;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.request.TaskAssignmentRequest;

import java.util.List;


public interface TaskService {
    MicroTask createAndAssignTask(TaskAssignmentRequest request, User mentor);
    List<Skill> getSuggestedSkills(Long internId); // Tính năng gợi ý Tags
}