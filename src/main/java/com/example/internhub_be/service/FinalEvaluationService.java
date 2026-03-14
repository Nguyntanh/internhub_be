package com.example.internhub_be.service;

import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.payload.request.FinalEvaluationRequest;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;

import java.util.List;

public interface FinalEvaluationService {

    List<UserResponse> getMyInterns(String mentorEmail);

    FinalEvaluationResponse getEvaluationByIntern(Long internId);

    FinalEvaluationResponse saveOrUpdateDraft(FinalEvaluationRequest request, String mentorEmail);

    FinalEvaluationResponse submitEvaluation(Long evaluationId, String mentorEmail);

    /** Mở khóa đánh giá để chỉnh sửa lại (reset về DRAFT, isLocked=false) */
    FinalEvaluationResponse resetEvaluation(Long evaluationId, String mentorEmail);
}