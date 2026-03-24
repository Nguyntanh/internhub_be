package com.example.internhub_be.payload;

import com.example.internhub_be.domain.MicroTask;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskReviewRequest {
    @NotNull(message = "Trạng thái đánh giá không được để trống")
    private MicroTask.MicroTaskStatus status; // REVIEWED | REJECTED
    private String reviewComment;
}