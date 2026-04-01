package com.example.internhub_be.repository;

import com.example.internhub_be.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByParentIdIsNull();
    List<Skill> findByParentId(Long parentId);
}
