package com.example.internhub_be.payload;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TaskSkillRatingResponse {
    private Long taskId;
    private Long skillId;
    private String skillName;
    private Integer weight;
    private BigDecimal ratingScore;
    private String reviewComment;
}