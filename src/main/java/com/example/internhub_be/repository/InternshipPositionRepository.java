package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipPositionRepository extends JpaRepository<InternshipPosition, Long> {
}