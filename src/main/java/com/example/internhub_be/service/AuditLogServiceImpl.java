package com.example.internhub_be.service;

import com.example.internhub_be.domain.AuditLog;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.AuditLogFilterRequest;
import com.example.internhub_be.payload.AuditLogResponse;
import com.example.internhub_be.payload.PagedResponse;
import com.example.internhub_be.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // ─── WRITE ───────────────────────────────────────────────────────────────

    @Override
    public void logAction(String action, User performedBy, Map<String, Object> details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUser(performedBy);

        try {
            auditLog.setDetails(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            System.err.println("Error converting audit log details to JSON: " + e.getMessage());
            auditLog.setDetails("{\"error\":\"Failed to convert details to JSON\"}");
        }

        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(this::extractClientIp)
                .ifPresent(auditLog::setIpAddress);

        auditLogRepository.save(auditLog);
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(AuditLogFilterRequest filter) {
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(direction, filter.getSortBy()));

        Page<AuditLog> page = auditLogRepository.findByFilters(
                filter.getUserId(),
                filter.getAction(),
                filter.getFromDate(),
                filter.getToDate(),
                pageable
        );

        // Filter keyword trên details (JSON string) ở tầng Java
        List<AuditLogResponse> content = page.getContent().stream()
                .filter(a -> filter.getKeyword() == null || filter.getKeyword().isBlank()
                        || (a.getDetails() != null && a.getDetails()
                            .toLowerCase().contains(filter.getKeyword().toLowerCase())))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return mapToResponse(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctActions() {
        return auditLogRepository.findDistinctActions();
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private AuditLogResponse mapToResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setAction(log.getAction());
        response.setDetails(log.getDetails());
        response.setIpAddress(log.getIpAddress());
        response.setCreatedAt(log.getCreatedAt());

        if (log.getUser() != null) {
            response.setUserId(log.getUser().getId());
            response.setUserName(log.getUser().getName());
            response.setUserEmail(log.getUser().getEmail());
        }
        return response;
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}