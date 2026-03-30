package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipDecisionRepository extends JpaRepository<InternshipDecision, Long> {

    // Tìm quyết định theo internshipProfile
    Optional<InternshipDecision> findByInternshipProfileId(Long internshipProfileId);

    // Tìm quyết định theo intern (user) ID — dùng JOIN qua InternshipProfile
    @Query("""
        SELECT d FROM InternshipDecision d
        JOIN FETCH d.internshipProfile p
        JOIN FETCH p.user u
        WHERE u.id = :internUserId
    """)
    Optional<InternshipDecision> findByInternUserId(@Param("internUserId") Long internUserId);

    // Danh sách quyết định của một Manager
    List<InternshipDecision> findByManagerId(Long managerId);

    // Danh sách quyết định chưa thông báo HR
    List<InternshipDecision> findByHrNotifiedFalse();

    // Kiểm tra hồ sơ đã có quyết định chưa
    boolean existsByInternshipProfileId(Long internshipProfileId);
}