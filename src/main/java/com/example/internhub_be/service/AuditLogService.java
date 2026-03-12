package com.example.internhub_be.service;

import com.example.internhub_be.domain.User;

import java.util.Map;

public interface AuditLogService {
    void logAction(String action, User performedBy, Map<String, Object> details);
}
