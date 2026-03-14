package com.example.internhub_be.service;

import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.payload.request.FinalEvaluationRequest;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;

import java.util.List;

public interface FinalEvaluationService {

    /** Lấy danh sách intern mà mentor đang phụ trách */
    List<UserResponse> getMyInterns(String mentorEmail);

    /** Lấy bảng điểm tổng hợp + trạng thái đánh giá hiện tại của một intern */
    FinalEvaluationResponse getEvaluationByIntern(Long internId);

    /** Tạo mới hoặc cập nhật draft đánh giá (chưa gửi) */
    FinalEvaluationResponse saveOrUpdateDraft(FinalEvaluationRequest request, String mentorEmail);

    /** Gửi phê duyệt — khóa tất cả micro_tasks của intern đó */
    FinalEvaluationResponse submitEvaluation(Long evaluationId, String mentorEmail);
}