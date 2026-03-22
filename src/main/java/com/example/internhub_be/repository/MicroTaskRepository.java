package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MicroTaskRepository extends JpaRepository<MicroTask, Long> {

    List<MicroTask> findByInternId(Long internId);

    List<MicroTask> findByIntern(User intern);

    List<MicroTask> findByMentor(User mentor);

    // ✅ Thêm method này — MentorTaskServiceImpl cần để lấy task đã submitted
    List<MicroTask> findByMentorAndStatus(User mentor, MicroTask.MicroTaskStatus status);
}