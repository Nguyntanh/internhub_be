package com.example.internhub_be.controller;

import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.payload.MicroTaskResponse;
import com.example.internhub_be.payload.TaskSkillRatingResponse;
import com.example.internhub_be.payload.TaskSubmissionRequest;
import com.example.internhub_be.repository.TaskSkillRatingRepository;
import com.example.internhub_be.service.InternTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/intern/tasks")
@PreAuthorize("hasRole('INTERN')")
public class InternTaskController {

    private final InternTaskService internTaskService;
    private final TaskSkillRatingRepository taskSkillRatingRepository;

    public InternTaskController(InternTaskService internTaskService,
                                TaskSkillRatingRepository taskSkillRatingRepository) {
        this.internTaskService = internTaskService;
        this.taskSkillRatingRepository = taskSkillRatingRepository;
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

    /**
     * GET /api/intern/tasks/{taskId}/skill-ratings
     * Intern xem kết quả đánh giá kỹ năng từ Mentor sau khi task được Reviewed.
     */
    @GetMapping("/{taskId}/skill-ratings")
    public ResponseEntity<List<TaskSkillRatingResponse>> getSkillRatings(
            @PathVariable Long taskId,
            Authentication authentication) {
        // Xác minh intern sở hữu task này (sẽ throw 403 nếu không phải)
        internTaskService.getMyTaskById(taskId, authentication.getName());

        List<TaskSkillRating> ratings = taskSkillRatingRepository.findByMicroTaskId(taskId);

        List<TaskSkillRatingResponse> response = ratings.stream().map(r -> {
            TaskSkillRatingResponse res = new TaskSkillRatingResponse();
            res.setTaskId(r.getMicroTask().getId());
            res.setSkillId(r.getSkill().getId());
            res.setSkillName(r.getSkill().getName());
            res.setWeight(r.getWeight());
            res.setRatingScore(r.getRatingScore());
            res.setReviewComment(r.getReviewComment());
            return res;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}