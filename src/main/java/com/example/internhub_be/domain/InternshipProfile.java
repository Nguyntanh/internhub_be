package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "internship_profiles")
public class InternshipProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_profile_user"))
    private User user;

    private String university;

    @Column(length = 100)
    private String major;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('In_Progress', 'Completed', 'Extended', 'Terminated') DEFAULT 'In_Progress'")
    private InternshipStatus status = InternshipStatus.In_Progress;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", foreignKey = @ForeignKey(name = "fk_profile_position"))
    private InternshipPosition position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", foreignKey = @ForeignKey(name = "fk_profile_mentor"))
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", foreignKey = @ForeignKey(name = "fk_profile_manager"))
    private User manager;

    // Internal Enum for Internship Status
    public enum InternshipStatus {
        In_Progress, Completed, Extended, Terminated
    }
}
