package com.opsflow.service;

import com.opsflow.entity.Notification;
import com.opsflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(Long userId, Long requestId, String message, String type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .requestId(requestId)
                .message(message)
                .type(type)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new com.opsflow.exception.ResourceNotFoundException(
                        "Notification not found with id: " + id));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
}
