package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InternshipProfileRepository extends JpaRepository<InternshipProfile, Long> {

    @Query("""
        SELECT p FROM InternshipProfile p
        JOIN FETCH p.user u
        LEFT JOIN FETCH u.department
        LEFT JOIN FETCH p.university
        LEFT JOIN FETCH p.position pos
        LEFT JOIN FETCH pos.department
        LEFT JOIN FETCH p.mentor
        LEFT JOIN FETCH p.manager
    """)
    List<InternshipProfile> findAllWithRelations();

    @Query("""
        SELECT p FROM InternshipProfile p
        JOIN FETCH p.user u
        LEFT JOIN FETCH u.department
        LEFT JOIN FETCH p.university
        LEFT JOIN FETCH p.position pos
        LEFT JOIN FETCH pos.department
        LEFT JOIN FETCH p.mentor
        LEFT JOIN FETCH p.manager
        WHERE p.id = :id
    """)
    Optional<InternshipProfile> findByIdWithRelations(@Param("id") Long id);

    List<InternshipProfile> findByMentorId(Long mentorId);

    List<InternshipProfile> findByManagerId(Long managerId);
}