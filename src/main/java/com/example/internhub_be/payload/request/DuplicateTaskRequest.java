package com.example.internhub_be.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DuplicateTaskRequest {

    /** Danh sách intern nhận task mới (bắt buộc ít nhất 1) */
    @NotEmpty(message = "Intern list cannot be empty")
    private List<Long> internIds;

    /** Deadline mới cho task (bắt buộc) */
    @NotNull(message = "Deadline cannot be null")
    private LocalDateTime deadline;
}