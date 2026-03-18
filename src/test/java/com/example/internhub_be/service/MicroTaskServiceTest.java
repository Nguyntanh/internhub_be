package com.example.internhub_be.service;

import com.example.internhub_be.domain.*;
import com.example.internhub_be.payload.request.CreateMicroTaskRequest;
import com.example.internhub_be.payload.request.SkillWeightRequest;
import com.example.internhub_be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MicroTaskServiceTest {

    @Mock
    private MicroTaskRepository microTaskRepository;
    @Mock
    private TaskSkillRatingRepository taskSkillRatingRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private MicroTaskServiceImpl microTaskService;

    @BeforeEach
    void setUp() {
        // Mock SecurityContextHolder for authenticated user
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTask_shouldCreateTaskSuccessfully() {
        // Given
        String mentorEmail = "mentor@example.com";
        Long mentorId = 1L;
        User mentor = new User();
        mentor.setId(mentorId);
        mentor.setEmail(mentorEmail);
        mentor.setName("Mentor User");

        Long internId = 2L;
        User intern = new User();
        intern.setId(internId);
        intern.setEmail("intern@example.com");
        intern.setName("Intern User");

        Long skillId = 3L;
        Skill skill = new Skill();
        skill.setId(skillId);
        skill.setName("Java");

        CreateMicroTaskRequest request = new CreateMicroTaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Description for test task");
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setInternIds(Collections.singletonList(internId));
        request.setSkills(Collections.singletonList(new SkillWeightRequest()));

        MicroTask savedTask = new MicroTask();
        savedTask.setId(10L); // simulate ID generated after save
        savedTask.setStatus(MicroTask.MicroTaskStatus.Todo); // Default status

        // Mocking
        when(authentication.getName()).thenReturn(mentorEmail);
        when(userRepository.findByEmail(mentorEmail)).thenReturn(Optional.of(mentor));
        when(userRepository.findById(internId)).thenReturn(Optional.of(intern));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(microTaskRepository.saveAndFlush(any(MicroTask.class))).thenReturn(savedTask);

        // When
        microTaskService.createTask(request);

        // Then
        verify(microTaskRepository, times(1)).saveAndFlush(any(MicroTask.class));
        verify(taskSkillRatingRepository, times(1)).save(any(TaskSkillRating.class));
        // Further assertions could involve capturing the arguments passed to saveAndFlush
        // and verifying their properties, e.g., task.getTitle(), task.getMentor(), etc.
    }

    @Test
    void createTask_shouldThrowRuntimeExceptionIfMentorNotFound() {
        // Given
        String mentorEmail = "nonexistent@example.com";
        CreateMicroTaskRequest request = new CreateMicroTaskRequest();
        request.setInternIds(Collections.singletonList(1L)); // Intern ID doesn't matter here

        // Mocking
        when(authentication.getName()).thenReturn(mentorEmail);
        when(userRepository.findByEmail(mentorEmail)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> microTaskService.createTask(request));
        assertEquals("Mentor not found", exception.getMessage());
        verify(microTaskRepository, never()).saveAndFlush(any(MicroTask.class));
    }

    @Test
    void createTask_shouldThrowRuntimeExceptionIfInternNotFound() {
        // Given
        String mentorEmail = "mentor@example.com";
        User mentor = new User();
        mentor.setEmail(mentorEmail);

        Long nonexistentInternId = 99L;
        CreateMicroTaskRequest request = new CreateMicroTaskRequest();
        request.setTitle("Test Task");
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setInternIds(Collections.singletonList(nonexistentInternId));

        // Mocking
        when(authentication.getName()).thenReturn(mentorEmail);
        when(userRepository.findByEmail(mentorEmail)).thenReturn(Optional.of(mentor));
        when(userRepository.findById(nonexistentInternId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> microTaskService.createTask(request));
        assertEquals("Intern not found", exception.getMessage());
        verify(microTaskRepository, never()).saveAndFlush(any(MicroTask.class));
    }

    @Test
    void createTask_shouldThrowRuntimeExceptionIfSkillNotFound() {
        // Given
        String mentorEmail = "mentor@example.com";
        User mentor = new User();
        mentor.setEmail(mentorEmail);

        Long internId = 2L;
        User intern = new User();
        intern.setId(internId);

        Long nonexistentSkillId = 99L;
        CreateMicroTaskRequest request = new CreateMicroTaskRequest();
        request.setTitle("Test Task");
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setInternIds(Collections.singletonList(internId));
        request.setSkills(Collections.singletonList(new SkillWeightRequest()));

        MicroTask savedTask = new MicroTask(); // Needs to be mocked even if skill not found
        savedTask.setId(10L);

        // Mocking
        when(authentication.getName()).thenReturn(mentorEmail);
        when(userRepository.findByEmail(mentorEmail)).thenReturn(Optional.of(mentor));
        when(userRepository.findById(internId)).thenReturn(Optional.of(intern));
        when(skillRepository.findById(nonexistentSkillId)).thenReturn(Optional.empty());
        when(microTaskRepository.saveAndFlush(any(MicroTask.class))).thenReturn(savedTask); // Task is saved before skill lookup

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> microTaskService.createTask(request));
        assertEquals("Skill not found", exception.getMessage());
        verify(microTaskRepository, times(1)).saveAndFlush(any(MicroTask.class)); // Task should be saved before skill error
        verify(taskSkillRatingRepository, never()).save(any(TaskSkillRating.class));
    }
}
