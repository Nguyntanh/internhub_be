package com.example.internhub_be.service;

import com.example.internhub_be.payload.MicroTaskResponse;
import com.example.internhub_be.payload.TaskSubmissionRequest;
import java.util.List;

public interface InternTaskService {
    List<MicroTaskResponse> getMyTasks(String internEmail);
    MicroTaskResponse getMyTaskById(Long taskId, String internEmail);
    MicroTaskResponse submitTask(Long taskId, TaskSubmissionRequest request, String internEmail);
}