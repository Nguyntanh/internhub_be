package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.TaskSkillRating;
import com.example.internhub_be.domain.TaskSkillRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSkillRatingRepository
        extends JpaRepository<TaskSkillRating, TaskSkillRatingId> {

    // ── Dùng cho MicroTaskServiceImpl (tìm theo taskId) ──────────────────────
    List<TaskSkillRating> findByMicroTaskId(Long taskId);

    // ── Dùng cho MentorTaskServiceImpl (tìm theo object MicroTask) ───────────
    List<TaskSkillRating> findByMicroTask(MicroTask microTask);
}