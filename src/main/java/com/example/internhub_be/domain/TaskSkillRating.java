package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_skill_ratings")
public class TaskSkillRating {

    @EmbeddedId
    private TaskSkillRatingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private MicroTask microTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "weight")
    private Integer weight = 1;

    @Column(name = "rating_score", precision = 3, scale = 2)
    private BigDecimal ratingScore;

    @Lob
    @Column(name = "review_comment")
    private String reviewComment;
}