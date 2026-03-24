package com.example.internhub_be.controller;

import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.dto.SkillDTO;
import com.example.internhub_be.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin
public class SkillController {

    private final SkillRepository repository;

    // ========================
    // GET ALL
    // ========================
    @GetMapping
    public List<SkillDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ========================
    // CREATE
    // ========================
    @PostMapping
    public SkillDTO create(@RequestBody SkillDTO dto) {

        Skill skill = new Skill();
        skill.setName(dto.name());
        skill.setDefaultWeight(dto.defaultWeight());

        if (dto.parentId() != null) {
            Skill parent = repository.findById(dto.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
            skill.setParent(parent);
        }

        Skill saved = repository.save(skill);
        return toDTO(saved);
    }

    // ========================
    // UPDATE
    // ========================
    @PutMapping("/{id}")
    public SkillDTO update(@PathVariable Long id,
                           @RequestBody SkillDTO dto) {

        Skill existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        existing.setName(dto.name());
        existing.setDefaultWeight(dto.defaultWeight());

        if (dto.parentId() != null) {
            Skill parent = repository.findById(dto.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
            existing.setParent(parent);
        } else {
            existing.setParent(null);
        }

        Skill saved = repository.save(existing);
        return toDTO(saved);
    }

    // ========================
    // DELETE
    // ========================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // ========================
    // MAPPER
    // ========================
    private SkillDTO toDTO(Skill skill) {
        return new SkillDTO(
                skill.getId(),
                skill.getName(),
                skill.getParent() != null ? skill.getParent().getId() : null,
                skill.getDefaultWeight()
        );
    }
}