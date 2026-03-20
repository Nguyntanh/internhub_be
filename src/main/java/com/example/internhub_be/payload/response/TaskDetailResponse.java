package com.example.internhub_be.payload.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDateTime deadline;

    private String status;

    private String submissionLink;

    private String submissionNote;

    private List<InternResponse> assignedInterns;

    private List<SkillRatingResponse> skills;

}