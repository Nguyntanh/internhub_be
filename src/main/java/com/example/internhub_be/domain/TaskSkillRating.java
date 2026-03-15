package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_skill_ratings")
public class TaskSkillRating {
    @EmbeddedId
    private TaskSkillRatingId id;

    @JsonIgnore // NGẮT VÒNG LẶP: Task -> Rating -> Task (Vòng lặp tử thần ở đây)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private MicroTask microTask;

    @ManyToOne(fetch = FetchType.EAGER) // Load nhanh kỹ năng khi xem task
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    private Integer weight = 1;

    @Column(name = "rating_score", precision = 3, scale = 2)
    private BigDecimal ratingScore;

    @Column(columnDefinition = "TEXT")
    private String reviewComment;
}