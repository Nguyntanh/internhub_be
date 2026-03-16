package com.example.internhub_be.payload;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AuditLogFilterRequest {

    private Long userId;           // Lọc theo người thực hiện
    private String action;         // Lọc theo loại hành động (e.g. USER_CREATED)
    private String keyword;        // Tìm kiếm trong details (JSON)

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate; // Lọc từ ngày

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;   // Lọc đến ngày

    // Pagination
    private int page = 0;
    private int size = 20;

    // Sorting
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}