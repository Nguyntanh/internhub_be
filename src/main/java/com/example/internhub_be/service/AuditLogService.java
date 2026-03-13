package com.example.internhub_be.service;

import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.AuditLogFilterRequest;
import com.example.internhub_be.payload.AuditLogResponse;
import com.example.internhub_be.payload.PagedResponse;

import java.util.List;
import java.util.Map;

public interface AuditLogService {

    /** Ghi một hành động vào audit log. */
    void logAction(String action, User performedBy, Map<String, Object> details);

    /** Truy xuất danh sách audit logs theo bộ lọc (phân trang). */
    PagedResponse<AuditLogResponse> getAuditLogs(AuditLogFilterRequest filter);

    /** Lấy chi tiết một audit log theo ID. */
    AuditLogResponse getAuditLogById(Long id);

    /** Lấy danh sách tất cả action types (dùng cho dropdown). */
    List<String> getDistinctActions();
}