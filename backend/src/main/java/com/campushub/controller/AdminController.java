package com.campushub.controller;

import com.campushub.dto.AdminTaskReviewRequest;
import com.campushub.dto.AdminAnnouncementRequest;
import com.campushub.dto.AdminFeedbackUpdateRequest;
import com.campushub.dto.AdminUserStatusUpdateRequest;
import com.campushub.dto.AdminVerificationReviewRequest;
import com.campushub.dto.UserVO;
import com.campushub.entity.Announcement;
import com.campushub.entity.Feedback;
import com.campushub.entity.Task;
import com.campushub.entity.UserVerification;
import com.campushub.service.AdminService;
import com.campushub.service.AnnouncementService;
import com.campushub.service.FeedbackService;
import com.campushub.service.VerificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final AnnouncementService announcementService;
    private final FeedbackService feedbackService;
    private final VerificationService verificationService;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    private Path uploadBasePath;

    public AdminController(AdminService adminService, AnnouncementService announcementService,
                           FeedbackService feedbackService, VerificationService verificationService) {
        this.adminService = adminService;
        this.announcementService = announcementService;
        this.feedbackService = feedbackService;
        this.verificationService = verificationService;
    }

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(uploadDir);
        }
        this.uploadBasePath = path.normalize().toAbsolutePath();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(Authentication authentication) {
        return adminService.getDashboard(getCurrentUserId(authentication));
    }

    @GetMapping("/users")
    public List<UserVO> getUsers(Authentication authentication) {
        return adminService.getUsers(getCurrentUserId(authentication));
    }

    @PutMapping("/users/{userId}/status")
    public UserVO updateUserStatus(
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

    @GetMapping("/verifications")
    public List<UserVerification> getVerifications(Authentication authentication) {
        adminService.requireAdmin(getCurrentUserId(authentication));
        return verificationService.getAllVerifications();
    }

    @PutMapping("/verifications/{id}/review")
    public UserVerification reviewVerification(
        @PathVariable Long id,
        @RequestBody AdminVerificationReviewRequest request,
        Authentication authentication
    ) {
        adminService.requireAdmin(getCurrentUserId(authentication));
        return verificationService.review(getCurrentUserId(authentication), id, request.getStatus(), request.getRejectReason());
    }

    @PutMapping("/verifications/{id}/revoke")
    public UserVerification revokeVerification(
        @PathVariable Long id,
        Authentication authentication
    ) {
        adminService.requireAdmin(getCurrentUserId(authentication));
        return verificationService.revoke(getCurrentUserId(authentication), id);
    }

    @GetMapping("/verifications/{id}/images/{filename}")
    public ResponseEntity<Resource> getVerificationImage(
        @PathVariable Long id,
        @PathVariable String filename,
        Authentication authentication
    ) {
        adminService.requireAdmin(getCurrentUserId(authentication));
        var verification = verificationService.getAllVerifications().stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("认证记录不存在"));

        var filePath = uploadBasePath.resolve("verifications").resolve(String.valueOf(verification.getUserId())).resolve(filename);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new RuntimeException("图片文件不存在");
        }

        String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .body(resource);
    }

    @PutMapping("/users/batch-status")
    public Map<String, Object> batchUpdateUserStatus(@RequestBody Map<String, Object> body, Authentication authentication) {
        return adminService.batchUpdateUserStatus(getCurrentUserId(authentication), body);
    }

    @PutMapping("/tasks/batch-review")
    public Map<String, Object> batchReviewTasks(@RequestBody Map<String, Object> body, Authentication authentication) {
        return adminService.batchReviewTasks(getCurrentUserId(authentication), body);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
