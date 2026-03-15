package com.example.internhub_be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String status;
    private String internName;
    private LocalDateTime deadline;
    // Chỉ lấy những gì Mentor cần thấy
}