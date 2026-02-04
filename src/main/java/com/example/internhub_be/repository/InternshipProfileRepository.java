package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipProfileRepository extends JpaRepository<InternshipProfile, Integer> {
}
