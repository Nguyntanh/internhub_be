package com.example.internhub_be.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String action;
    private String details;    // JSON string
    private String ipAddress;
    private LocalDateTime createdAt;
}