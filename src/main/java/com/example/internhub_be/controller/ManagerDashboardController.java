package com.example.internhub_be.controller;

import com.example.internhub_be.payload.response.ManagerInternSummaryResponse;
import com.example.internhub_be.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/comparison")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ManagerInternSummaryResponse>> getInternsComparison() {
        return ResponseEntity.ok(dashboardService.getManagerDashboard());
    }
}