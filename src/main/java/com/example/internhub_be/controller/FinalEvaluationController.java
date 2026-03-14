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

    /**
     * GET /api/mentor/interns
     * Trả về danh sách intern mà mentor hiện tại đang phụ trách
     * (dựa vào internship_profiles.mentor_id)
     */
    @GetMapping("/interns")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponse>> getMyInterns() {
        return ResponseEntity.ok(evaluationService.getMyInterns(getCurrentUserEmail()));
    }

    /**
     * GET /api/mentor/evaluations/intern/{internId}
     * Mentor xem bảng điểm tổng hợp + trạng thái đánh giá của intern
     */
    @GetMapping("/evaluations/intern/{internId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<FinalEvaluationResponse> getEvaluationSummary(
            @PathVariable Long internId) {
        return ResponseEntity.ok(evaluationService.getEvaluationByIntern(internId));
    }

    /**
     * POST /api/mentor/evaluations
     * Tạo mới hoặc cập nhật draft đánh giá
     */
    @PostMapping("/evaluations")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> saveOrUpdateDraft(
            @Valid @RequestBody FinalEvaluationRequest request) {
        FinalEvaluationResponse response = evaluationService.saveOrUpdateDraft(request, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/mentor/evaluations/{id}/submit
     * Gửi phê duyệt — không thể hoàn tác
     */
    @PostMapping("/evaluations/{id}/submit")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> submitEvaluation(
            @PathVariable Long id) {
        FinalEvaluationResponse response = evaluationService.submitEvaluation(id, getCurrentUserEmail());
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}