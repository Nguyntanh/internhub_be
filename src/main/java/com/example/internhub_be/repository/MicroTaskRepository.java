package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.MicroTask.MicroTaskStatus;
import com.example.internhub_be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MicroTaskRepository extends JpaRepository<MicroTask, Integer> {
    List<MicroTask> findByIntern(User intern);
    List<MicroTask> findByInternAndStatus(User intern, MicroTaskStatus status);
    List<MicroTask> findByMentor(User mentor);
    List<MicroTask> findByMentorAndStatus(User mentor, MicroTaskStatus status);
}