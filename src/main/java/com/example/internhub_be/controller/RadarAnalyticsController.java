package com.example.internhub_be.controller;

import com.example.internhub_be.payload.response.RadarAnalyticsResponse;
import com.example.internhub_be.service.RadarAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * GET /api/radar/intern/{internId}
 *
 * Quyền: ADMIN, HR, MANAGER, MENTOR, INTERN
 * Kiểm tra phân quyền chi tiết (ai được xem ai) xử lý trong RadarAnalyticsService.
 */
@RestController
@RequestMapping("/api/radar")
@RequiredArgsConstructor
public class RadarAnalyticsController {

    private final RadarAnalyticsService radarAnalyticsService;

    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR', 'INTERN')")
    public ResponseEntity<RadarAnalyticsResponse> getRadarByIntern(
            @PathVariable Long internId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(
                radarAnalyticsService.getRadarByIntern(internId, auth.getName())
        );
    }
}