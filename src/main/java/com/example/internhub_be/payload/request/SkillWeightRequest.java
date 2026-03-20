package com.example.internhub_be.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SkillWeightRequest {

    private Long skillId;

    @Min(1)
    @Max(5)
    private Integer weight;
}