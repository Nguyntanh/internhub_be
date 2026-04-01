package com.example.internhub_be.payload.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkillRatingResponse {

    private Long skillId;

    private String skillName;

    private Integer weight;

    private BigDecimal ratingScore;

    private String reviewComment;

}