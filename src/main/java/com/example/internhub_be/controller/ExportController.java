package com.example.internhub_be.controller;

import com.example.internhub_be.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * GET /api/export/intern/{id}/excel
     * Xuất báo cáo 1 intern: thông tin định danh + biểu đồ năng lực + nhận xét Mentor + quyết định Manager
     * Roles: ADMIN, HR, MANAGER, MENTOR
     */
    @GetMapping("/intern/{id}/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public ResponseEntity<byte[]> exportInternExcel(@PathVariable Long id) throws IOException {
        String email = getCurrentEmail();
        byte[] content = exportService.exportInternReport(id, email);

        String filename = "bao-cao-intern-" + id + "-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    /**
     * GET /api/export/group/excel?departmentId=1&universityId=2
     * Xuất báo cáo nhóm intern — lọc theo phòng ban hoặc trường đại học (tùy chọn)
     * Roles: ADMIN, HR
     */
    @GetMapping("/group/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<byte[]> exportGroupExcel(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long universityId) throws IOException {

        String email = getCurrentEmail();
        byte[] content = exportService.exportGroupReport(departmentId, universityId, email);

        String filename = "bao-cao-nhom-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    private String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}