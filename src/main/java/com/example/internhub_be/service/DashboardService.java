package com.example.internhub_be.service;

import com.example.internhub_be.payload.response.InternDashboardResponse;
import com.example.internhub_be.payload.response.ManagerInternSummaryResponse;

import java.util.List;

public interface DashboardService {
    InternDashboardResponse getInternDashboard(Long userId);
    List<ManagerInternSummaryResponse> getManagerDashboard();
}
