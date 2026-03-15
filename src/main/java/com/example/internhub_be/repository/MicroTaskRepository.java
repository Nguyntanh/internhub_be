package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MicroTaskRepository extends JpaRepository<MicroTask, Long> {

    // ── Tìm theo internId (dùng cho MicroTaskServiceImpl cũ) ──────────────────
    List<MicroTask> findByInternId(Long internId);

    // ── Tìm theo object User (dùng cho InternTaskServiceImpl) ─────────────────
    List<MicroTask> findByIntern(User intern);

    // ── Tìm theo mentor object (dùng cho MentorTaskServiceImpl) ──────────────
    List<MicroTask> findByMentor(User mentor);

    // ── Tìm theo mentor + status (dùng cho getSubmittedTasks) ────────────────
    List<MicroTask> findByMentorAndStatus(User mentor, MicroTask.MicroTaskStatus status);

    // ── Tìm theo taskSkillRating (dùng cho TaskSkillRatingRepository helper) ──
    List<MicroTask> findByMentorId(Long mentorId);
}