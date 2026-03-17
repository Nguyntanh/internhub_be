package com.example.internhub_be.service;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.payload.request.*;
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

    void deleteTask(Long taskId); // thêm dòng này

    void updateTask(Long taskId, UpdateMicroTaskRequest request);

}