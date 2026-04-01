package com.example.internhub_be.controller;

import com.example.internhub_be.payload.response.InternDashboardResponse;
import com.example.internhub_be.security.UserPrincipal;
import com.example.internhub_be.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/intern")
@RequiredArgsConstructor
public class InternDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<InternDashboardResponse> getInternDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        Long internId = currentUser.getId();
        InternDashboardResponse dashboardResponse = dashboardService.getInternDashboard(internId);
        return ResponseEntity.ok(dashboardResponse);
    }
}
