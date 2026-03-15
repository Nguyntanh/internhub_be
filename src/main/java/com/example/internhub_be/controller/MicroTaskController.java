package com.example.internhub_be.controller;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.payload.request.ReviewTaskRequest;
import com.example.internhub_be.payload.request.SubmitTaskRequest;
import com.example.internhub_be.payload.response.TaskDetailResponse;
import com.example.internhub_be.service.MicroTaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class MicroTaskController {

    private final MicroTaskService microTaskService;

    @GetMapping("/intern/{internId}")
    public List<MicroTask> getTasksByIntern(@PathVariable Long internId) {

        return microTaskService.getTasksByIntern(internId);

    }

    @GetMapping("/{taskId}")
    public TaskDetailResponse getTaskDetail(@PathVariable Long taskId){

        return microTaskService.getTaskDetail(taskId);

    }

    @PostMapping("/{taskId}/submit")
    public String submitTask(
            @PathVariable Long taskId,
            @RequestBody SubmitTaskRequest request
    ){
        microTaskService.submitTask(taskId, request);

        return "Task submitted successfully";
    }

    @PostMapping("/{taskId}/review")
    public String reviewTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ReviewTaskRequest request
    ){
        microTaskService.reviewTask(taskId, request);

        return "Task reviewed successfully";
    }
}