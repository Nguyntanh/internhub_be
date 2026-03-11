package com.example.internhub_be.payload;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TaskSkillRatingRequest {
    @NotNull(message = "ID kỹ năng không được để trống")
    private Long skillId;
    @Min(value = 1, message = "Trọng số tối thiểu là 1")
    private Integer weight = 1;
    @NotNull(message = "Điểm đánh giá không được để trống")
    @DecimalMin(value = "0.00", message = "Điểm tối thiểu là 0.00")
    @DecimalMax(value = "5.00", message = "Điểm tối đa là 5.00")
    private BigDecimal ratingScore;
    private String reviewComment;
}