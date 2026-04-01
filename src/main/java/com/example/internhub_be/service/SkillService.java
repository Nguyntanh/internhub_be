package com.example.internhub_be.service;

import com.example.internhub_be.domain.Skill;
import com.example.internhub_be.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository repository;

    public List<Skill> getAll() {
        return repository.findAll();
    }

    public Skill save(Skill skill) {
        return repository.save(skill);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}