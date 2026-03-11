package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.InternshipProfile.InternshipStatus;
import com.example.internhub_be.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InternshipProfileRepository extends JpaRepository<InternshipProfile, Long> {

    Page<InternshipProfile> findByStatus(InternshipStatus status, Pageable pageable);

    @Query("SELECT p FROM InternshipProfile p WHERE LOWER(p.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<InternshipProfile> findByUserNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM InternshipProfile p WHERE (LOWER(p.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.status = :status")
    Page<InternshipProfile> findByUserNameContainingIgnoreCaseAndStatus(@Param("keyword") String keyword, @Param("status") InternshipStatus status, Pageable pageable);

    List<InternshipProfile> findByMentor(User mentor);

    List<InternshipProfile> findByManager(User manager);

    long countByMentor(User mentor);

    long countByManager(User manager);
}
