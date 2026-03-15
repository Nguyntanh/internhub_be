package com.example.internhub_be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String status;
    private String internName;
    private LocalDateTime deadline;
    private List<SkillWeightResponse> skills;

    @Data
    @AllArgsConstructor
    public static class SkillWeightResponse {
        private String skillName;
        private Integer weight; // Trọng số của kỹ năng này trong task
    }
}