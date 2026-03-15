package com.example.internhub_be.repository;

import com.example.internhub_be.domain.FinalEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalEvaluationRepository extends JpaRepository<FinalEvaluation, Long> {

    Optional<FinalEvaluation> findByInternId(Long internId);

    boolean existsByInternId(Long internId);
}
