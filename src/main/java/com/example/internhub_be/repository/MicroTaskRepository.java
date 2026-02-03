package com.example.internhub_be.repository;

import com.example.internhub_be.domain.MicroTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicroTaskRepository extends JpaRepository<MicroTask, Integer> {
}
