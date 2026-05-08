package com.campushub.controller;

import com.campushub.dto.AdminTaskReviewRequest;
import com.campushub.dto.AdminAnnouncementRequest;
import com.campushub.dto.AdminFeedbackUpdateRequest;
import com.campushub.dto.AdminUserStatusUpdateRequest;
import com.campushub.entity.Announcement;
import com.campushub.entity.Feedback;
import com.campushub.entity.Task;
import com.campushub.entity.User;
import com.campushub.service.AdminService;
import com.campushub.service.AnnouncementService;
import com.campushub.service.FeedbackService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final AnnouncementService announcementService;
    private final FeedbackService feedbackService;

    public AdminController(AdminService adminService, AnnouncementService announcementService, FeedbackService feedbackService) {
        this.adminService = adminService;
        this.announcementService = announcementService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(Authentication authentication) {
        return adminService.getDashboard(getCurrentUserId(authentication));
    }

    @GetMapping("/users")
    public List<User> getUsers(Authentication authentication) {
        return adminService.getUsers(getCurrentUserId(authentication));
    }

    @PutMapping("/users/{userId}/status")
    public User updateUserStatus(
        @PathVariable Long userId,
        @RequestBody AdminUserStatusUpdateRequest request,
        Authentication authentication
    ) {
        return adminService.updateUserStatus(getCurrentUserId(authentication), userId, request);
    }

    @GetMapping("/tasks")
    public List<Task> getTasks(Authentication authentication) {
        return adminService.getTasks(getCurrentUserId(authentication));
    }

    @PutMapping("/tasks/{taskId}/review")
    public Task reviewTask(
        @PathVariable Long taskId,
        @RequestBody AdminTaskReviewRequest request,
        Authentication authentication
    ) {
        return adminService.reviewTask(getCurrentUserId(authentication), taskId, request);
    }

    @GetMapping("/announcements")
    public List<Announcement> getAnnouncements(Authentication authentication) {
        adminService.requireAdmin(getCurrentUserId(authentication));
        return announcementService.getAnnouncements();
    }

    @PostMapping("/announcements")
    public Announcement createAnnouncement(
        @RequestBody AdminAnnouncementRequest request,
        Authentication authentication
    ) {
        return announcementService.createAnnouncement(getCurrentUserId(authentication), request);
    }

    @GetMapping("/feedback")
    public List<Feedback> getFeedback(Authentication authentication) {
        return feedbackService.getAdminFeedback(getCurrentUserId(authentication));
    }

    @PutMapping("/feedback/{feedbackId}")
    public Feedback updateFeedback(
        @PathVariable Long feedbackId,
        @RequestBody AdminFeedbackUpdateRequest request,
        Authentication authentication
    ) {
        return feedbackService.updateFeedback(getCurrentUserId(authentication), feedbackId, request);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
