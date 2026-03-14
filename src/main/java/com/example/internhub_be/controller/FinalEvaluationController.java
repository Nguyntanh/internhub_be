package com.example.internhub_be.controller;

import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.payload.request.FinalEvaluationRequest;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;
import com.example.internhub_be.service.FinalEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class FinalEvaluationController {

    private final FinalEvaluationService evaluationService;

    @GetMapping("/interns")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponse>> getMyInterns() {
        return ResponseEntity.ok(evaluationService.getMyInterns(getCurrentUserEmail()));
    }

    @GetMapping("/evaluations/intern/{internId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<FinalEvaluationResponse> getEvaluationSummary(
            @PathVariable Long internId) {
        return ResponseEntity.ok(evaluationService.getEvaluationByIntern(internId));
    }

    @PostMapping("/evaluations")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> saveOrUpdateDraft(
            @Valid @RequestBody FinalEvaluationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluationService.saveOrUpdateDraft(request, getCurrentUserEmail()));
    }

    @PostMapping("/evaluations/{id}/submit")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> submitEvaluation(
            @PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.submitEvaluation(id, getCurrentUserEmail()));
    }

    /** POST /api/mentor/evaluations/{id}/reset — mở khóa để đánh giá lại */
    @PostMapping("/evaluations/{id}/reset")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> resetEvaluation(
            @PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.resetEvaluation(id, getCurrentUserEmail()));
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}