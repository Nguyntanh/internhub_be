package com.example.internhub_be.controller;

import com.example.internhub_be.payload.AuditLogFilterRequest;
import com.example.internhub_be.payload.AuditLogResponse;
import com.example.internhub_be.payload.PagedResponse;
import com.example.internhub_be.service.AuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * GET /api/admin/audit-logs
     * Truy xuất danh sách audit logs với bộ lọc và phân trang.
     *
     * Query params (tất cả optional):
     *   userId    - ID người thực hiện hành động
     *   action    - Loại hành động (VD: USER_CREATED, USER_STATUS_UPDATED)
     *   keyword   - Từ khóa tìm trong details
     *   fromDate  - Từ ngày (ISO 8601, VD: 2025-01-01T00:00:00)
     *   toDate    - Đến ngày (ISO 8601)
     *   page      - Số trang (mặc định: 0)
     *   size      - Kích thước trang (mặc định: 20)
     *   sortBy    - Trường sắp xếp (mặc định: createdAt)
     *   sortDir   - Chiều sắp xếp: asc | desc (mặc định: desc)
     */
    @GetMapping
    public ResponseEntity<PagedResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        AuditLogFilterRequest filter = new AuditLogFilterRequest();
        filter.setUserId(userId);
        filter.setAction(action);
        filter.setKeyword(keyword);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(auditLogService.getAuditLogs(filter));
    }

    /**
     * GET /api/admin/audit-logs/{id}
     * Xem chi tiết một audit log.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    /**
     * GET /api/admin/audit-logs/actions
     * Lấy danh sách tất cả loại action đang có trong hệ thống (dùng cho dropdown filter).
     */
    @GetMapping("/actions")
    public ResponseEntity<List<String>> getDistinctActions() {
        return ResponseEntity.ok(auditLogService.getDistinctActions());
    }
}