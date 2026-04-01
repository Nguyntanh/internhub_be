package com.example.internhub_be.service;

import com.example.internhub_be.payload.request.ManagerDecisionRequest;
import com.example.internhub_be.payload.response.ManagerReviewResponse;

import java.util.List;

public interface ManagerReviewService {

    /**
     * Lấy danh sách các hồ sơ intern cần Manager xem xét
     * (FinalEvaluation đã SUBMITTED, chưa có quyết định hoặc đã có quyết định).
     *
     * @param managerEmail email của Manager đang đăng nhập
     * @param pendingOnly  true = chỉ lấy chưa có quyết định; false = lấy tất cả
     */
    List<ManagerReviewResponse> getPendingReviews(String managerEmail, boolean pendingOnly);

    /**
     * Lấy báo cáo tổng hợp đầy đủ cho một intern cụ thể.
     * Bao gồm: thông tin intern, nhận xét Mentor, điểm kỹ năng, lịch sử task.
     *
     * @param internshipProfileId ID của InternshipProfile
     * @param managerEmail        email của Manager (dùng để kiểm tra quyền)
     */
    ManagerReviewResponse getReviewDetail(Long internshipProfileId, String managerEmail);

    /**
     * Manager ra quyết định: PASS, FAIL, hoặc CONVERT_TO_STAFF.
     * Sau khi lưu, tự động gửi thông báo cho HR.
     *
     * @param request      request chứa internshipProfileId + decision + comment
     * @param managerEmail email của Manager đang đăng nhập
     */
    ManagerReviewResponse submitDecision(ManagerDecisionRequest request, String managerEmail);
}