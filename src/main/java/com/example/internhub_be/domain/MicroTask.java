package com.example.internhub_be.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "micro_tasks")
public class MicroTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Todo', 'In_Progress', 'Submitted', 'Reviewed', 'Rejected') DEFAULT 'Todo'")
    private MicroTaskStatus status = MicroTaskStatus.Todo;

    @Lob
    @Column(name = "submission_link")
    private String submissionLink;

    @Lob
    @Column(name = "submission_note")
    private String submissionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User intern;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MicroTaskStatus {
        Todo,
        In_Progress,
        Submitted,
        Reviewed,
        Rejected
    }
}