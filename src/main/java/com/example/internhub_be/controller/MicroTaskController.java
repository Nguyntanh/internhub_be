package com.example.internhub_be.controller;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.payload.request.ReviewTaskRequest;
import com.example.internhub_be.payload.request.SubmitTaskRequest;
import com.example.internhub_be.payload.response.TaskDetailResponse;
import com.example.internhub_be.service.MicroTaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cũ — giữ lại để tương thích ngược nhưng đã được bảo vệ bằng auth.
 * Ưu tiên dùng:
 *   - /api/intern/tasks  (InternTaskController)  cho Intern
 *   - /api/mentor/tasks  (MentorTaskController)  cho Mentor
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MicroTaskController {

    private final MicroTaskService microTaskService;

    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER', 'HR')")
    public List<MicroTask> getTasksByIntern(@PathVariable Long internId) {
        return microTaskService.getTasksByIntern(internId);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER', 'INTERN')")
    public TaskDetailResponse getTaskDetail(@PathVariable Long taskId) {
        return microTaskService.getTaskDetail(taskId);
    }

    @PostMapping("/{taskId}/submit")
    @PreAuthorize("hasRole('INTERN')")
    public String submitTask(
            @PathVariable Long taskId,
            @RequestBody SubmitTaskRequest request
    ) {
        microTaskService.submitTask(taskId, request);
        return "Task submitted successfully";
    }

    @PostMapping("/{taskId}/review")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String reviewTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ReviewTaskRequest request
    ) {
        microTaskService.reviewTask(taskId, request);
        return "Task reviewed successfully";
    }
}