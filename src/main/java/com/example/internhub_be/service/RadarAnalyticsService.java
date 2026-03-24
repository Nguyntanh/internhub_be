package com.example.internhub_be.service;

import com.example.internhub_be.payload.response.RadarAnalyticsResponse;

/**
 * Tổng hợp điểm kỹ năng Radar cho Intern.
 * Đối chiếu SkillCore (thực tế từ task_skill_ratings) với Benchmark (tiêu chuẩn vị trí).
 */
public interface RadarAnalyticsService {

    /**
     * @param internId       ID của intern cần xem
     * @param requesterEmail Email người gọi API — dùng để kiểm tra quyền trong service
     */
    RadarAnalyticsResponse getRadarByIntern(Long internId, String requesterEmail);
}