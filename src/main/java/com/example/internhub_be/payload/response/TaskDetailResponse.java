package com.example.internhub_be.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class TaskDetailResponse {

    private Long id;

    private String title;

    private String description;

    private String status;

    private List<TaskSkillResponse> skills;

}