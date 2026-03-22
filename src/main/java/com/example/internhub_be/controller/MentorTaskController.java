package com.example.internhub_be.controller;

import com.example.internhub_be.payload.*;
import com.example.internhub_be.service.MentorTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mentor/tasks")
@PreAuthorize("hasRole('MENTOR')")
public class MentorTaskController {

    private final MentorTaskService mentorTaskService;

    public MentorTaskController(MentorTaskService mentorTaskService) {
        this.mentorTaskService = mentorTaskService;
    }

    @GetMapping
    public ResponseEntity<List<MicroTaskResponse>> getMyAssignedTasks(Authentication authentication) {
        return ResponseEntity.ok(mentorTaskService.getTasksAssignedByMe(authentication.getName()));
    }

    @GetMapping("/submitted")
    public ResponseEntity<List<MicroTaskResponse>> getSubmittedTasks(Authentication authentication) {
        return ResponseEntity.ok(mentorTaskService.getSubmittedTasks(authentication.getName()));
    }

    @PatchMapping("/{taskId}/review")
    public ResponseEntity<MicroTaskResponse> reviewTask(@PathVariable Long taskId,
                                                         @Valid @RequestBody TaskReviewRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(mentorTaskService.reviewTask(taskId, request, authentication.getName()));
    }

    @PostMapping("/{taskId}/skill-ratings")
    public ResponseEntity<List<TaskSkillRatingResponse>> rateTaskSkills(
            @PathVariable Long taskId,
            @Valid @RequestBody List<TaskSkillRatingRequest> ratings,
            Authentication authentication) {
        return ResponseEntity.ok(mentorTaskService.rateTaskSkills(taskId, ratings, authentication.getName()));
    }

    @GetMapping("/{taskId}/skill-ratings")
    public ResponseEntity<List<TaskSkillRatingResponse>> getTaskSkillRatings(
            @PathVariable Long taskId, Authentication authentication) {
        return ResponseEntity.ok(mentorTaskService.getTaskSkillRatings(taskId, authentication.getName()));
    }
}