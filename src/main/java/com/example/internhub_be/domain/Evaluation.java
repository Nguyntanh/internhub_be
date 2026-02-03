package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evaluations")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", unique = true, foreignKey = @ForeignKey(name = "fk_eval_task"))
    private MicroTask task;

    @Column(precision = 3, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String review;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Task_Grade', 'Mid_Term', 'Final_Term') DEFAULT 'Task_Grade'")
    private EvaluationType type = EvaluationType.Task_Grade;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Internal Enum for Evaluation Type
    public enum EvaluationType {
        Task_Grade, Mid_Term, Final_Term
    }
}
