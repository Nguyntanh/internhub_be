package com.example.internhub_be.repository;

import com.example.internhub_be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email); // Changed from findByUsername
    Optional<User> findByActivationToken(String activationToken);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department d " + // Added department fetch
           "LEFT JOIN FETCH u.internshipProfile ip " +
           "LEFT JOIN FETCH ip.university uni " +
           "LEFT JOIN FETCH ip.position pos " +
           "LEFT JOIN FETCH ip.mentor mentor " +
           "WHERE u.email = :email")
    Optional<User> findUserWithProfileByEmail(String email);

}
