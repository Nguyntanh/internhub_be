package com.example.internhub_be.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskSubmissionRequest {
    @NotBlank(message = "Link nộp bài không được để trống")
    private String submissionLink;
    private String submissionNote;
}