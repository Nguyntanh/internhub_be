package com.example.internhub_be.service;

import com.example.internhub_be.payload.MicroTaskResponse;
import com.example.internhub_be.payload.TaskReviewRequest;
import com.example.internhub_be.payload.TaskSkillRatingRequest;
import com.example.internhub_be.payload.TaskSkillRatingResponse;
import java.util.List;

public interface MentorTaskService {
    List<MicroTaskResponse> getTasksAssignedByMe(String mentorEmail);
    List<MicroTaskResponse> getSubmittedTasks(String mentorEmail);
    MicroTaskResponse reviewTask(Long taskId, TaskReviewRequest request, String mentorEmail);
    List<TaskSkillRatingResponse> rateTaskSkills(Long taskId, List<TaskSkillRatingRequest> ratings, String mentorEmail);
    List<TaskSkillRatingResponse> getTaskSkillRatings(Long taskId, String mentorEmail);
}