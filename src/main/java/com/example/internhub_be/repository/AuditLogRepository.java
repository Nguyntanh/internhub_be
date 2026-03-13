package com.example.internhub_be.repository;

import com.example.internhub_be.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        LEFT JOIN a.user u
        WHERE (:userId   IS NULL OR u.id       = :userId)
          AND (:action   IS NULL OR a.action   = :action)
          AND (:fromDate IS NULL OR a.createdAt >= :fromDate)
          AND (:toDate   IS NULL OR a.createdAt <= :toDate)
        """)
    Page<AuditLog> findByFilters(
            @Param("userId")   Long userId,
            @Param("action")   String action,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a.action FROM AuditLog a ORDER BY a.action")
    List<String> findDistinctActions();
}