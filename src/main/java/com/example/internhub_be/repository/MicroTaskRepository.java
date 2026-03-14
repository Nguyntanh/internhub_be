package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MicroTaskRepository extends JpaRepository<MicroTask, Long> {
    List<MicroTask> findByInternId(Long internId);
}