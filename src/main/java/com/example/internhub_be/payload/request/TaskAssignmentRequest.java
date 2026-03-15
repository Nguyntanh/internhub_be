package com.example.internhub_be.payload.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskAssignmentRequest {
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Integer weight; // Trọng số nhiệm vụ (1-5)
    private Long internId; // ID của Intern nhận việc
    private List<SkillRatingRequest> skills; // Danh sách ít nhất 01 Sub-tag

    @Data
    public static class SkillRatingRequest {
        private Long skillId;
        private Integer weight; // Trọng số kỹ năng trong task này
    }
}