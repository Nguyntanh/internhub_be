package com.example.internhub_be.repository;

import com.example.internhub_be.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findByNameIgnoreCase(String name);
}
