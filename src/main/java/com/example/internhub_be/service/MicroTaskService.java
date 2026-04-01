package com.example.internhub_be.service;

import com.example.internhub_be.payload.request.CreateMicroTaskRequest;
import com.example.internhub_be.payload.request.DuplicateTaskRequest;
import com.example.internhub_be.payload.request.ReviewTaskRequest;
import com.example.internhub_be.payload.request.SubmitTaskRequest;
import com.example.internhub_be.payload.request.UpdateMicroTaskRequest;
import com.example.internhub_be.payload.response.TaskDetailResponse;
import com.example.internhub_be.payload.response.TaskResponse;

import java.util.List;

public interface MicroTaskService {

    void createTask(CreateMicroTaskRequest request);

    List<TaskResponse> getTasksForCurrentIntern();

    List<TaskResponse> getTasksForCurrentMentor();

    TaskDetailResponse getTaskDetail(Long taskId);

    void submitTask(Long taskId, SubmitTaskRequest request);

    void reviewTask(Long taskId, ReviewTaskRequest request);

    void deleteTask(Long taskId);

    void updateTask(Long taskId, UpdateMicroTaskRequest request);

    // ✅ MỚI: Duplicate task sang intern mới với deadline mới.
    // Giữ nguyên title, description, skill tags, weight từ task gốc.
    List<TaskDetailResponse> duplicateTask(Long taskId, DuplicateTaskRequest request);
}