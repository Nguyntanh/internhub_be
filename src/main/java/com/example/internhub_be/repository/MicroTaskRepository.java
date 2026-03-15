package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import org.springframework.data.jpa.repository.JpaRepository;  
import java.util.List;

public interface MicroTaskRepository extends JpaRepository<MicroTask, Long> {
    List<MicroTask> findByMentorId(Long mentorId);
}
