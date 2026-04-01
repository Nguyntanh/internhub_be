package com.example.internhub_be.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MilestoneResponse {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private String status; // COMPLETED, IN_PROGRESS, TODO
}