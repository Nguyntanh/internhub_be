package com.example.internhub_be.controller;

import com.example.internhub_be.payload.MicroTaskResponse;
import com.example.internhub_be.payload.TaskSubmissionRequest;
import com.example.internhub_be.service.InternTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/intern/tasks")
@PreAuthorize("hasRole('INTERN')")
public class InternTaskController {

    private final InternTaskService internTaskService;

    public InternTaskController(InternTaskService internTaskService) {
        this.internTaskService = internTaskService;
    }

    @GetMapping
    public ResponseEntity<List<MicroTaskResponse>> getMyTasks(Authentication authentication) {
        return ResponseEntity.ok(internTaskService.getMyTasks(authentication.getName()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<MicroTaskResponse> getMyTaskById(@PathVariable Long taskId,
                                                            Authentication authentication) {
        return ResponseEntity.ok(internTaskService.getMyTaskById(taskId, authentication.getName()));
    }

    @PostMapping("/{taskId}/submit")
    public ResponseEntity<MicroTaskResponse> submitTask(@PathVariable Long taskId,
                                                         @Valid @RequestBody TaskSubmissionRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(internTaskService.submitTask(taskId, request, authentication.getName()));
    }
}