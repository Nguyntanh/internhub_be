package com.example.internhub_be.controller;

import com.example.internhub_be.payload.request.DepartmentRequest;
import com.example.internhub_be.payload.response.DepartmentResponse;
import com.example.internhub_be.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * GET /api/departments
     * Lấy danh sách tất cả phòng ban.
     * Endpoint này public theo cấu hình SecurityConfig.
     */
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    /**
     * GET /api/departments/{id}
     * Lấy chi tiết một phòng ban.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    /**
     * POST /api/departments
     * Tạo mới phòng ban (Chỉ ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request));
    }

    /**
     * PUT /api/departments/{id}
     * Cập nhật phòng ban (Chỉ ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    /**
     * DELETE /api/departments/{id}
     * Xóa phòng ban (Chỉ ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Member management ────────────────────────────────────────────────────────

    /**
     * POST /api/departments/{id}/members
     * Body: { "userId": 5 }
     * Adds a user to this department.
     */
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<DepartmentResponse> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        return ResponseEntity.ok(departmentService.addMember(id, userId));
    }

    /**
     * DELETE /api/departments/{id}/members/{userId}
     * Removes (unassigns) a user from this department.
     */
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<DepartmentResponse> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(departmentService.removeMember(id, userId));
    }

    /**
     * PATCH /api/departments/members/{userId}/move
     * Body: { "targetDepartmentId": 3 }   (null = unassign)
     * Moves a user to a different department.
     */
    @PatchMapping("/members/{userId}/move")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Void> moveMember(
            @PathVariable Long userId,
            @RequestBody Map<String, Long> body) {
        Long targetDeptId = body.get("targetDepartmentId");
        departmentService.moveMember(userId, targetDeptId);
        return ResponseEntity.noContent().build();
    }
}