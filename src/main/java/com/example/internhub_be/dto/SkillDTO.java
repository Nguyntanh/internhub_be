package com.example.internhub_be.dto;

public record SkillDTO(
        Long id,
        String name,
        Long parentId,
        Integer defaultWeight
) {}