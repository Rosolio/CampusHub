package com.campushub.controller;

import com.campushub.entity.Notification;
import com.campushub.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getNotifications(Authentication authentication) {
        return notificationService.getNotifications(getCurrentUserId(authentication));
    }

    @GetMapping("/unread-count")
    public Map<String, Integer> getUnreadCount(Authentication authentication) {
        return Map.of("count", notificationService.getUnreadCount(getCurrentUserId(authentication)));
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Authentication authentication) {
        notificationService.markAsRead(id, getCurrentUserId(authentication));
    }

    @PutMapping("/read-all")
    public void markAllRead(Authentication authentication) {
        notificationService.markAllRead(getCurrentUserId(authentication));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
