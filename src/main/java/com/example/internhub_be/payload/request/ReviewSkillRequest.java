package com.example.internhub_be.payload.request;

import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class ReviewSkillRequest {

    private Long skillId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0")
    @DecimalMax(value = "10.0", message = "Score must be <= 10")
    private BigDecimal score;

    private String comment;
}