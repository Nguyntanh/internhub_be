
package com.example.internhub_be.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinalEvaluationRequest {

    @NotNull(message = "internId không được để trống")
    private Long internId;

    @NotBlank(message = "Nhận xét tổng kết không được để trống")
    private String overallComment;
}
