package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.request.DepartmentRequest;
import com.example.internhub_be.payload.response.DepartmentResponse;
import com.example.internhub_be.payload.response.InternshipPositionResponse;
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return convertToResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Phòng ban '" + request.getName() + "' đã tồn tại");
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        // [ĐÃ XÓA] setSamplePositions — không còn dùng ElementCollection

        Department saved = departmentRepository.save(department);
        assignLeaders(request.getLeaderIds(), saved);
        return convertToResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Kiểm tra trùng tên với phòng ban KHÁC
        departmentRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Phòng ban '" + request.getName() + "' đã tồn tại");
            }
        });

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        // [ĐÃ XÓA] setSamplePositions — không còn dùng ElementCollection

        assignLeaders(request.getLeaderIds(), department);
        return convertToResponse(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        // cascade = CascadeType.ALL trên positions → tự động xóa InternshipPositions liên kết
        departmentRepository.delete(department);
    }

    private void assignLeaders(List<Long> leaderIds, Department dept) {
        if (leaderIds != null && !leaderIds.isEmpty()) {
            List<User> leaders = userRepository.findAllById(leaderIds);
            leaders.forEach(u -> u.setDepartment(dept));
            userRepository.saveAll(leaders);
        }
    }

    private InternshipPositionResponse convertPositionToResponse(
            com.example.internhub_be.domain.InternshipPosition p) {
        return InternshipPositionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .departmentId(p.getDepartment() != null ? p.getDepartment().getId() : null)
                .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
                .build();
    }

    private DepartmentResponse convertToResponse(Department department) {
        List<InternshipPositionResponse> positions =
                (department.getPositions() != null)
                        ? department.getPositions().stream()
                        .map(this::convertPositionToResponse)
                        .collect(Collectors.toList())
                        : Collections.emptyList();

        List<String> memberNames =
                (department.getMembers() != null)
                        ? department.getMembers().stream()
                        .map(User::getName)
                        .collect(Collectors.toList())
                        : Collections.emptyList();

        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .positions(positions)
                .memberNames(memberNames)
                .build();
    }
}