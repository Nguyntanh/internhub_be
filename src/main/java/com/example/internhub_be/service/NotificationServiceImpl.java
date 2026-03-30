package com.example.internhub_be.service;

import com.example.internhub_be.domain.Notification;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.PagedResponse;
import com.example.internhub_be.payload.response.NotificationResponse;
import com.example.internhub_be.repository.NotificationRepository;
import com.example.internhub_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ─── TẠO THÔNG BÁO ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void createNotification(User recipient, User sender,
                                   Notification.NotificationType type,
                                   String title, String message,
                                   Long referenceId, String referenceType) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setSender(sender);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceId(referenceId);
        n.setReferenceType(referenceType);
        n.setIsRead(false);
        notificationRepository.save(n);
    }

    // ─── DANH SÁCH ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getMyNotifications(String email, int page, int size) {
        User user = getUserByEmail(email);
        Page<Notification> pg = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));

        return new PagedResponse<>(
                pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                pg.getNumber(),
                pg.getSize(),
                pg.getTotalElements(),
                pg.getTotalPages(),
                pg.isLast()
        );
    }

    // ─── ĐẾM CHƯA ĐỌC ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long countUnread(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    // ─── ĐÁNH DẤU ĐÃ ĐỌC ─────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String email) {
        User user = getUserByEmail(email);
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!n.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền đọc thông báo này.");
        }

        if (!Boolean.TRUE.equals(n.getIsRead())) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
        return mapToResponse(n);
    }

    // ─── ĐÁNH DẤU TẤT CẢ ĐÃ ĐỌC ──────────────────────────────────────────

    @Override
    @Transactional
    public int markAllAsRead(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.markAllReadByRecipient(user.getId());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .senderId(n.getSender() != null ? n.getSender().getId() : null)
                .senderName(n.getSender() != null ? n.getSender().getName() : null)
                .senderAvatar(n.getSender() != null ? n.getSender().getAvatar() : null)
                .build();
    }
}