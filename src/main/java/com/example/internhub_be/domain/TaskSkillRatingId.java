package com.example.internhub_be.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TaskSkillRatingId implements Serializable {

    private Long taskId;

    private Long skillId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskSkillRatingId)) return false;
        TaskSkillRatingId that = (TaskSkillRatingId) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, skillId);
    }
}