package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.request.DepartmentRequest;
import com.example.internhub_be.payload.response.DepartmentResponse;
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Added missing import

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department1;
    private Department department2;
    private DepartmentRequest departmentRequest;

    @BeforeEach
    void setUp() {
        department1 = new Department();
        department1.setId(1L);
        department1.setName("IT");
        department1.setDescription("Information Technology Department");

        department2 = new Department();
        department2.setId(2L);
        department2.setName("HR");
        department2.setDescription("Human Resources Department");

        departmentRequest = new DepartmentRequest();
        departmentRequest.setName("Finance");
        departmentRequest.setDescription("Finance Department");
        departmentRequest.setLeaderIds(Collections.emptyList());
    }

    @Test
    void testGetAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2));

        List<DepartmentResponse> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("IT", result.get(0).getName());
        assertEquals("HR", result.get(1).getName());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetDepartmentById() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));

        DepartmentResponse result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals("IT", result.getName());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateDepartment() {
        when(departmentRepository.findByName(departmentRequest.getName())).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setId(3L); // Simulate ID generation
            return dept;
        });

        DepartmentResponse result = departmentService.createDepartment(departmentRequest);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Finance", result.getName());
        verify(departmentRepository, times(1)).findByName("Finance");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testCreateDepartment_NameAlreadyExists() {
        when(departmentRepository.findByName(departmentRequest.getName())).thenReturn(Optional.of(new Department()));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            departmentService.createDepartment(departmentRequest);
        });

        assertTrue(thrown.getMessage().contains("đã tồn tại"));
        verify(departmentRepository, times(1)).findByName("Finance");
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testUpdateDepartment() {
        Department existingDepartment = new Department();
        existingDepartment.setId(1L);
        existingDepartment.setName("Old Name");
        existingDepartment.setDescription("Old Description");

        DepartmentRequest updateRequest = new DepartmentRequest();
        updateRequest.setName("New Name");
        updateRequest.setDescription("New Description");
        updateRequest.setLeaderIds(Collections.emptyList());

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.findByName(updateRequest.getName())).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(existingDepartment);

        DepartmentResponse result = departmentService.updateDepartment(1L, updateRequest);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).findByName("New Name");
        verify(departmentRepository, times(1)).save(existingDepartment);
    }

    @Test
    void testDeleteDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department1));
        doNothing().when(departmentRepository).delete(department1);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).delete(department1);
    }

    @Test
    void testCreateDepartmentAndGetAllDepartments() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setId(100L); // Simulate ID assigned by DB
        newDepartment.setName("New Dept");
        newDepartment.setDescription("New Department Description");

        DepartmentRequest createRequest = new DepartmentRequest();
        createRequest.setName("New Dept");
        createRequest.setDescription("New Department Description");
        createRequest.setLeaderIds(Collections.emptyList());

        // Mock behavior for createDepartment
        when(departmentRepository.findByName(createRequest.getName())).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(newDepartment);

        // When createDepartment is called
        departmentService.createDepartment(createRequest);

        // Then, immediately call getAllDepartments
        // Simulate findAll returning the new department along with others if any
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2, newDepartment));
        List<DepartmentResponse> allDepartments = departmentService.getAllDepartments();

        // Verify
        assertNotNull(allDepartments);
        assertEquals(3, allDepartments.size()); // Should include the new department
        assertTrue(allDepartments.stream().anyMatch(d -> d.getName().equals("New Dept")));
        verify(departmentRepository, times(1)).findByName("New Dept");
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(departmentRepository, times(1)).findAll();
    }
}

