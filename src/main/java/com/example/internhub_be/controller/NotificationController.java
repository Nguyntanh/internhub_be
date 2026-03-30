package com.example.internhub_be.controller;

import com.example.internhub_be.payload.PagedResponse;
import com.example.internhub_be.payload.response.NotificationResponse;
import com.example.internhub_be.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API thông báo nội bộ — dùng cho tất cả role.
 *
 * Base URL: /api/notifications
 *
 * Endpoints:
 *   GET  /              - Danh sách thông báo của tôi (phân trang)
 *   GET  /unread-count  - Số thông báo chưa đọc
 *   PATCH /{id}/read    - Đánh dấu 1 thông báo đã đọc
 *   PATCH /read-all     - Đánh dấu tất cả đã đọc
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications?page=0&size=20
     *
     * Trả về danh sách thông báo của user hiện tại, mới nhất lên đầu.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(getCurrentEmail(), page, size));
    }

    /**
     * GET /api/notifications/unread-count
     *
     * Dùng để hiển thị badge số lượng trên icon chuông.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.countUnread(getCurrentEmail());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * PATCH /api/notifications/{id}/read
     *
     * Đánh dấu 1 thông báo cụ thể là đã đọc.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id, getCurrentEmail()));
    }

    /**
     * PATCH /api/notifications/read-all
     *
     * Đánh dấu tất cả thông báo của user hiện tại là đã đọc.
     * Trả về số lượng thông báo được cập nhật.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        int updated = notificationService.markAllAsRead(getCurrentEmail());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}