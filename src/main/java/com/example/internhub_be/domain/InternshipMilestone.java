package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "internship_milestones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days")
    private Integer durationDays; // Số ngày dự kiến cho giai đoạn này

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private InternshipPosition position;
}