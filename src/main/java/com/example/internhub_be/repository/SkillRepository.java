package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipPosition;
import com.example.internhub_be.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    // Removed the old findByPosition method
    // List<Skill> findByPosition(InternshipPosition position);

    // New method to find skills associated with an InternshipPosition
    // This assumes that 'internshipPositions' is the field name in the Skill entity
    Set<Skill> findByInternshipPositions(InternshipPosition position);
}
