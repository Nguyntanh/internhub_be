package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "micro_tasks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MicroTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private MicroTaskStatus status = MicroTaskStatus.Todo;

    private String submissionLink;
    private String submissionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id") // Nếu giao cho nhóm, chuyển cái này thành Set<User> assignees
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User intern;

    // QUAN TRỌNG: Kết nối với các kỹ năng đã giao
    @OneToMany(mappedBy = "microTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskSkillRating> skillRatings = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MicroTaskStatus { Todo, In_Progress, Submitted, Reviewed, Rejected }
}