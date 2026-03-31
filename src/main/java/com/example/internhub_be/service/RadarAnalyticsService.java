package com.example.internhub_be.service;

import com.example.internhub_be.payload.response.RadarAnalyticsResponse;

public interface RadarAnalyticsService {

    RadarAnalyticsResponse getRadarByIntern(Long internId, String requesterEmail);

    RadarAnalyticsResponse getRadarForExport(Long internId);
}