package com.example.internhub_be.payload.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewSkillRequest {

    private Long skillId;

    private BigDecimal ratingScore;

    private String reviewComment;

}