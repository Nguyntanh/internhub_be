package com.example.internhub_be.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private String status;

    private LocalDateTime deadline;
}