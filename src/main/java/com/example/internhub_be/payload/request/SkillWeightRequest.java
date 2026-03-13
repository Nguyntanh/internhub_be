package com.example.internhub_be.payload.request;

import lombok.Data;

@Data
public class SkillWeightRequest {

    private Long skillId;

    private Integer weight;
}