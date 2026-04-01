package com.example.internhub_be.payload.request;

import com.example.internhub_be.domain.InternshipDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManagerDecisionRequest {

    /**
     * ID của InternshipProfile (hồ sơ thực tập).
     * Manager chọn hồ sơ nào để ra quyết định.
     */
    @NotNull(message = "ID hồ sơ thực tập không được để trống")
    private Long internshipProfileId;

    /**
     * Quyết định: PASS, FAIL, hoặc CONVERT_TO_STAFF
     */
    @NotNull(message = "Quyết định không được để trống")
    private InternshipDecision.DecisionType decision;

    /**
     * Nhận xét của Manager (tuỳ chọn).
     */
    private String managerComment;
}