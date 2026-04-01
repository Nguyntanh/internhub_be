package com.example.internhub_be.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipPositionResponse {
    private Long id;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
}