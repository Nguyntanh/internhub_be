package com.example.internhub_be.payload;

import com.example.internhub_be.domain.MicroTask;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MicroTaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private MicroTask.MicroTaskStatus status;
    private String submissionLink;
    private String submissionNote;
    private Long mentorId;
    private String mentorName;
    private Long internId;
    private String internName;
    private LocalDateTime createdAt;
}