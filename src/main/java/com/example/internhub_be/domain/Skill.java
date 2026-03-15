package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // Removed parent/children relationship for skills as it's not relevant for this fix.
    // @JsonBackReference // Ngắt vòng lặp: Khi xem Sub-tag sẽ không render ngược lại toàn bộ thông tin Parent
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "parent_id")
    // private Skill parent;

    // @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    // private List<Skill> children;

    @Column(name = "default_weight")
    private Integer defaultWeight = 1;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "internship_position_skills",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "position_id")
    )
    private Set<InternshipPosition> internshipPositions;
}