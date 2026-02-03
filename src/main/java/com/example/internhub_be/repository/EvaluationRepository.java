package com.example.internhub_be.repository;

import com.example.internhub_be.domain.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
}
