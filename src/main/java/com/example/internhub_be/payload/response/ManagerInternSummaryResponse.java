package com.example.internhub_be.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerInternSummaryResponse {
    private Long internId;
    private String fullName;
    private String positionName;
    private Double gpa;              // Lấy từ FinalEvaluation
    private Double completionRate;   // Tỷ lệ hoàn thành Task (%)
    private String status;           // Trạng thái: Đạt/Không đạt (PASS/FAIL)
}