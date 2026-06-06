package com.campushub.service;

import com.campushub.entity.Notification;
import com.campushub.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public void createNotification(Long userId, String type, String title, String content,
                                    String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationMapper.selectByUserId(userId, 50);
    }

    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }
        if (!userId.equals(notification.getUserId())) {
            throw new RuntimeException("无权操作该通知");
        }
        notificationMapper.updateReadStatus(id, true);
    }

    public void markAllRead(Long userId) {
        notificationMapper.markAllReadByUserId(userId);
    }
}
