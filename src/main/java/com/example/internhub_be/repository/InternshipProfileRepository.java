package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternshipProfileRepository extends JpaRepository<InternshipProfile, Long> {

    // Lấy tất cả intern mà một mentor đang phụ trách
    List<InternshipProfile> findByMentorId(Long mentorId);

    // Lấy theo manager
    List<InternshipProfile> findByManagerId(Long managerId);
}