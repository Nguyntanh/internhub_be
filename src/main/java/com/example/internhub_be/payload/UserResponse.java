package com.example.internhub_be.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private Long departmentId;
    private String departmentName;
    private Boolean isActive;
    private String phone;
    private String avatar;
    private LocalDateTime createdAt;
}
