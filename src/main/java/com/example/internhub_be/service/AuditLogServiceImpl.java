package com.example.internhub_be.service;

import com.example.internhub_be.domain.AuditLog;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper; // To convert Map to JSON string

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void logAction(String action, User performedBy, Map<String, Object> details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUser(performedBy);

        try {
            // Convert details map to JSON string
            auditLog.setDetails(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            // Log the error if JSON conversion fails
            System.err.println("Error converting audit log details to JSON: " + e.getMessage());
            auditLog.setDetails("{ \"error\": \"Failed to convert details to JSON\" }");
        }

        // Attempt to get client IP address from the request
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRemoteAddr)
                .ifPresent(auditLog::setIpAddress);

        auditLogRepository.save(auditLog);
    }
}
