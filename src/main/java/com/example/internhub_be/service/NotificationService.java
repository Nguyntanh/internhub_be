package com.example.internhub_be.service;

import com.example.internhub_be.domain.Notification;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.PagedResponse;
import com.example.internhub_be.payload.response.NotificationResponse;

public interface NotificationService {

    /**
     * Tạo và lưu một thông báo mới.
     *
     * @param recipient    Người nhận
     * @param sender       Người gửi (null nếu hệ thống tự tạo)
     * @param type         Loại thông báo
     * @param title        Tiêu đề
     * @param message      Nội dung chi tiết
     * @param referenceId  ID thực thể liên quan (FinalEvaluation, InternshipDecision...)
     * @param referenceType Tên loại thực thể
     */
    void createNotification(User recipient, User sender,
                            Notification.NotificationType type,
                            String title, String message,
                            Long referenceId, String referenceType);

    /**
     * Lấy danh sách thông báo của user hiện tại (phân trang).
     */
    PagedResponse<NotificationResponse> getMyNotifications(String email, int page, int size);

    /**
     * Đếm số thông báo chưa đọc của user.
     */
    long countUnread(String email);

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    NotificationResponse markAsRead(Long notificationId, String email);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc.
     */
    int markAllAsRead(String email);
}