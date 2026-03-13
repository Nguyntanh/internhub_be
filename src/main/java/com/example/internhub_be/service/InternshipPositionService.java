package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.InternshipPosition;
import com.example.internhub_be.payload.request.InternshipPositionRequest;
import com.example.internhub_be.payload.response.InternshipPositionResponse;
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.InternshipPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternshipPositionService {

    private final InternshipPositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    public InternshipPositionService(InternshipPositionRepository positionRepository,
                                     DepartmentRepository departmentRepository) {
        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Lấy toàn bộ vị trí hoặc lọc theo departmentId.
     * Nếu departmentId = null → trả toàn bộ (dùng cho Admin quản lý).
     * Nếu departmentId != null → trả theo phòng ban (dùng cho HR dropdown ở E02).
     */
    @Transactional(readOnly = true)
    public List<InternshipPositionResponse> getAllPositions(Long departmentId) {
        List<InternshipPosition> positions = (departmentId != null)
                ? positionRepository.findByDepartmentId(departmentId)
                : positionRepository.findAll();

        return positions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InternshipPositionResponse getPositionById(Long id) {
        InternshipPosition position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + id));
        return convertToResponse(position);
    }

    @Transactional
    public InternshipPositionResponse createPosition(InternshipPositionRequest request) {
        InternshipPosition position = new InternshipPosition();
        position.setName(request.getName());
        position.setDescription(request.getDescription());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: "
                            + request.getDepartmentId()));
            position.setDepartment(department);
        }

        return convertToResponse(positionRepository.save(position));
    }

    @Transactional
    public InternshipPositionResponse updatePosition(Long id, InternshipPositionRequest request) {
        InternshipPosition position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + id));

        position.setName(request.getName());
        position.setDescription(request.getDescription());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: "
                            + request.getDepartmentId()));
            position.setDepartment(department);
        } else {
            position.setDepartment(null);
        }

        return convertToResponse(positionRepository.save(position));
    }

    @Transactional
    public void deletePosition(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new RuntimeException("Position not found with id: " + id);
        }
        positionRepository.deleteById(id);
    }

    private InternshipPositionResponse convertToResponse(InternshipPosition position) {
        return InternshipPositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .description(position.getDescription())
                .departmentId(position.getDepartment() != null ? position.getDepartment().getId() : null)
                .departmentName(position.getDepartment() != null ? position.getDepartment().getName() : null)
                .build();
    }
}