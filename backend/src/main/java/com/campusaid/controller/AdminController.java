package com.campusaid.controller;

import com.campusaid.dto.AdminTaskReviewRequest;
import com.campusaid.dto.AdminUserStatusUpdateRequest;
import com.campusaid.entity.Task;
import com.campusaid.entity.User;
import com.campusaid.service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
