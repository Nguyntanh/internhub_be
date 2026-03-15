package com.example.internhub_be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.request.TaskAssignmentRequest;
import com.example.internhub_be.service.SecurityService;
import com.example.internhub_be.service.TaskService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SecurityService securityService; // Để lấy User hiện tại

    @PostMapping("/assign")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<?> assignTask(@RequestBody TaskAssignmentRequest request) {
        User currentMentor = securityService.getCurrentUser();
        MicroTask task = taskService.createAndAssignTask(request, currentMentor);
        return ResponseEntity.ok("Task assigned successfully with ID: " + task.getId());
    }

    @GetMapping("/suggestions/{internId}")
    public ResponseEntity<List<Skill>> getSuggestions(@PathVariable Long internId) {
        return ResponseEntity.ok(taskService.getSuggestedSkills(internId));
    }
}