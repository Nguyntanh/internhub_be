package com.example.internhub_be.payload;

import lombok.Data;

@Data
public class SupervisorResponse {
    private Long id;
    private String name;
    private String email;
    private String avatar;
    private String roleName;
    private String departmentName;
    private Long assignedInternCount;
}
