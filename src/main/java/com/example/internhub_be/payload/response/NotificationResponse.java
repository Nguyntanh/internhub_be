package com.example.internhub_be.payload.response;

import com.example.internhub_be.domain.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long                       id;
    private Notification.NotificationType type;
    private String                     title;
    private String                     message;
    private Long                       referenceId;
    private String                     referenceType;
    private Boolean                    isRead;
    private LocalDateTime              readAt;
    private LocalDateTime              createdAt;

    // Người gửi (nếu có)
    private Long   senderId;
    private String senderName;
    private String senderAvatar;
}