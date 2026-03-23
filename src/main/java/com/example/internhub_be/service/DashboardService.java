package com.example.internhub_be.service;

import com.example.internhub_be.payload.response.InternDashboardResponse;

public interface DashboardService {
    InternDashboardResponse getInternDashboard(Long internId);
}
