package com.example.internhub_be.service;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.payload.request.CreateMicroTaskRequest;
import com.example.internhub_be.payload.request.SubmitTaskRequest;
import com.example.internhub_be.payload.request.ReviewTaskRequest;
import com.example.internhub_be.payload.response.TaskDetailResponse;

import java.util.List;

public interface MicroTaskService {

    void createTask(CreateMicroTaskRequest request, Long mentorId);

    List<MicroTask> getTasksByIntern(Long internId);

    void submitTask(Long taskId, SubmitTaskRequest request);

    void reviewTask(Long taskId, ReviewTaskRequest request);

    TaskDetailResponse getTaskDetail(Long taskId);
}