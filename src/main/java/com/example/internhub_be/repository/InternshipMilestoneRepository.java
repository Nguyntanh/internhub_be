package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InternshipMilestoneRepository extends JpaRepository<InternshipMilestone, Long> {
    List<InternshipMilestone> findByPositionIdOrderByOrderIndexAsc(Long positionId);
}