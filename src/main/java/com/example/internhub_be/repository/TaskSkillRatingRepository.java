package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.TaskSkillRatingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskSkillRatingRepository
        extends JpaRepository<TaskSkillRating, TaskSkillRatingId> {

    List<TaskSkillRating> findByMicroTask(MicroTask microTask);

    List<TaskSkillRating> findByMicroTaskId(Long taskId); // ADD THIS
}