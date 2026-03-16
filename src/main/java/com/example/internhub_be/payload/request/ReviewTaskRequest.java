package com.example.internhub_be.payload.request;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
public class ReviewTaskRequest {

    @NotEmpty(message = "Skill list cannot be empty")
    @Valid
    private List<ReviewSkillRequest> skills;

}