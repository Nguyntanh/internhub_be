package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.request.DepartmentRequest;
import com.example.internhub_be.payload.response.DepartmentResponse;
import com.example.internhub_be.payload.response.InternshipPositionResponse;
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.InternshipProfileRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final InternshipProfileRepository internshipProfileRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             InternshipProfileRepository internshipProfileRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.internshipProfileRepository = internshipProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        // Load tất cả InternshipProfile 1 lần để tránh N+1 query
        Map<Long, InternshipProfile> profileByUserId = internshipProfileRepository
                .findAllWithRelations()
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        p -> p,
                        (a, b) -> a  // nếu trùng, giữ cái đầu
                ));

        return departmentRepository.findAll().stream()
                .map(dept -> convertToResponse(dept, profileByUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        Map<Long, InternshipProfile> profileByUserId = internshipProfileRepository
                .findAllWithRelations()
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        p -> p,
                        (a, b) -> a
                ));

        return convertToResponse(department, profileByUserId);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Phòng ban '" + request.getName() + "' đã tồn tại");
        }
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        Department saved = departmentRepository.save(department);
        assignLeaders(request.getLeaderIds(), saved);
        return convertToResponse(saved, Collections.emptyMap());
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        departmentRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Phòng ban '" + request.getName() + "' đã tồn tại");
            }
        });
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        assignLeaders(request.getLeaderIds(), department);
        return convertToResponse(departmentRepository.save(department), Collections.emptyMap());
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        if (department.getMembers() != null && !department.getMembers().isEmpty()) {
            List<User> members = department.getMembers();
            members.forEach(u -> u.setDepartment(null));
            userRepository.saveAll(members);
        }
        departmentRepository.delete(department);
    }

    @Transactional
    public DepartmentResponse addMember(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setDepartment(department);
        userRepository.save(user);
        Department refreshed = departmentRepository.findById(departmentId).get();
        return convertToResponse(refreshed, Collections.emptyMap());
    }

    @Transactional
    public DepartmentResponse removeMember(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (user.getDepartment() != null && user.getDepartment().getId().equals(departmentId)) {
            user.setDepartment(null);
            userRepository.save(user);
        }
        Department refreshed = departmentRepository.findById(departmentId).get();
        return convertToResponse(refreshed, Collections.emptyMap());
    }

    @Transactional
    public void moveMember(Long userId, Long targetDepartmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Department targetDept = (targetDepartmentId != null)
                ? departmentRepository.findById(targetDepartmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + targetDepartmentId))
                : null;
        user.setDepartment(targetDept);
        userRepository.save(user);
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

    private DepartmentResponse convertToResponse(Department department,
                                                 Map<Long, InternshipProfile> profileByUserId) {
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

        List<DepartmentResponse.MemberInfo> members =
                (department.getMembers() != null)
                        ? department.getMembers().stream()
                        .map(u -> {
                            // Lấy InternshipProfile nếu có
                            InternshipProfile profile = profileByUserId.get(u.getId());
                            String positionName = null;
                            String internDeptName = null;
                            if (profile != null) {
                                if (profile.getPosition() != null) {
                                    positionName = profile.getPosition().getName();
                                }
                                if (profile.getPosition() != null
                                        && profile.getPosition().getDepartment() != null) {
                                    internDeptName = profile.getPosition().getDepartment().getName();
                                }
                            }
                            return DepartmentResponse.MemberInfo.builder()
                                    .id(u.getId())
                                    .name(u.getName())
                                    .email(u.getEmail())
                                    .roleName(u.getRole() != null ? u.getRole().getName() : null)
                                    .positionName(positionName)
                                    .internDepartmentName(internDeptName)
                                    .build();
                        })
                        .collect(Collectors.toList())
                        : Collections.emptyList();

        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .positions(positions)
                .memberNames(memberNames)
                .members(members)
                .build();
    }
}