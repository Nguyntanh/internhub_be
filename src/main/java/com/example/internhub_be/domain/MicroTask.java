package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "micro_tasks")
public class MicroTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer weight = 1; // Default value from DDL

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Todo', 'In_Progress', 'Submitted', 'Reviewed', 'Rejected') DEFAULT 'Todo'")
    private MicroTaskStatus status = MicroTaskStatus.Todo; // Default value from DDL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", foreignKey = @ForeignKey(name = "fk_task_mentor"))
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", foreignKey = @ForeignKey(name = "fk_task_intern"))
    private User intern;

    @ManyToMany
    @JoinTable(
        name = "task_skill_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    // Internal Enum for MicroTask Status
    public enum MicroTaskStatus {
        Todo, In_Progress, Submitted, Reviewed, Rejected
    }
}
