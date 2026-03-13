package com.example.internhub_be.repository;

import com.example.internhub_be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Changed from findByUsername
    Optional<User> findByActivationToken(String activationToken);
}
