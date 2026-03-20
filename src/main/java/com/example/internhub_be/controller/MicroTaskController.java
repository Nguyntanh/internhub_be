package com.example.internhub_be.controller;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.payload.request.CreateMicroTaskRequest;
import com.example.internhub_be.payload.request.ReviewTaskRequest;
import com.example.internhub_be.payload.request.SubmitTaskRequest;
import com.example.internhub_be.payload.request.UpdateMicroTaskRequest;
import com.example.internhub_be.payload.response.TaskDetailResponse;
import com.example.internhub_be.payload.response.TaskResponse;
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

    // Mentor tạo task
    @PostMapping
    public String createTask(@Valid @RequestBody CreateMicroTaskRequest request) {

        microTaskService.createTask(request);

        return "Task created successfully";
    }

    // Intern xem task của mình
    @GetMapping("/intern")
    public List<TaskResponse> getMyTasks() {
        return microTaskService.getTasksForCurrentIntern();
    }

    // Mentor xem task đã giao
    @GetMapping("/mentor")
    public List<TaskResponse> getMentorTasks() {
        return microTaskService.getTasksForCurrentMentor();
    }

    // Chi tiết task
    @GetMapping("/{taskId}")
    public TaskDetailResponse getTaskDetail(@PathVariable Long taskId) {

        return microTaskService.getTaskDetail(taskId);
    }

    // Intern submit task
    @PostMapping("/{taskId}/submit")
    public String submitTask(
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitTaskRequest request
    ) {

        microTaskService.submitTask(taskId, request);

        return "Task submitted successfully";
    }

    // Mentor review task
    @PostMapping("/{taskId}/review")
    public String reviewTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ReviewTaskRequest request
    ) {

        microTaskService.reviewTask(taskId, request);

        return "Task reviewed successfully";
    }

    @DeleteMapping("/{taskId}")
    public String deleteTask(@PathVariable Long taskId) {

        microTaskService.deleteTask(taskId);

        return "Task deleted successfully";
    }

    @PutMapping("/{taskId}")
    public String updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateMicroTaskRequest request
    ) {

        microTaskService.updateTask(taskId, request);

        return "Task updated successfully";
    }

}