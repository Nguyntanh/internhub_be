package com.example.internhub_be.payload.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateMicroTaskRequest {

    private String title;

    private String description;

    private LocalDateTime deadline;

}