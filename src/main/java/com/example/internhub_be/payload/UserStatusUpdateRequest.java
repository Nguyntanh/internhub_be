package com.example.internhub_be.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;
}
