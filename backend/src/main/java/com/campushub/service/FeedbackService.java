package com.campushub.service;

import com.campushub.dto.AdminFeedbackUpdateRequest;
import com.campushub.dto.FeedbackCreateRequest;
import com.campushub.entity.Feedback;
import com.campushub.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final UserService userService;
    private final MessageService messageService;

    public FeedbackService(FeedbackMapper feedbackMapper, UserService userService, MessageService messageService) {
        this.feedbackMapper = feedbackMapper;
        this.userService = userService;
        this.messageService = messageService;
    }

    public Feedback createFeedback(Long userId, FeedbackCreateRequest request) {
        if (userService.isAdmin(userId)) {
            throw new RuntimeException("管理员无需通过社区反馈提交问题");
        }

        String title = trimToNull(request.getTitle());
        String content = trimToNull(request.getContent());
        if (title == null) {
            throw new RuntimeException("反馈标题不能为空");
        }
        if (content == null) {
            throw new RuntimeException("反馈内容不能为空");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        String type = normalizeType(request.getType());
        feedback.setType(type);
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setStatus("open");
        feedback.setPriority(normalizePriority(request.getPriority(), type));
        feedback.setAdminReply(null);
        feedback.setAdminId(null);
        feedback.setHandledAt(null);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return feedbackMapper.selectById(feedback.getId());
    }

    public List<Feedback> getMyFeedback(Long userId) {
        if (userService.isAdmin(userId)) {
            return Collections.emptyList();
        }
        return feedbackMapper.selectByUserId(userId);
    }

    public void withdrawFeedback(Long userId, Long feedbackId) {
        if (userService.isAdmin(userId)) {
            throw new RuntimeException("管理员无需撤回社区反馈");
        }
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null || !userId.equals(feedback.getUserId())) {
            throw new RuntimeException("反馈不存在");
        }
        if ("resolved".equals(feedback.getStatus())) {
            throw new RuntimeException("已解决的反馈不能撤回");
        }
        feedbackMapper.delete(feedbackId);
    }

    public List<Feedback> getAdminFeedback(Long adminId) {
        requireAdmin(adminId);
        return feedbackMapper.selectAll();
    }

    public Feedback updateFeedback(Long adminId, Long feedbackId, AdminFeedbackUpdateRequest request) {
        requireAdmin(adminId);
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        String previousStatus = feedback.getStatus();
        String previousReply = trimToNull(feedback.getAdminReply());
        String nextReply = trimToNull(request.getAdminReply());
        String nextStatus = normalizeStatus(request.getStatus());
        String nextPriority = request.getPriority() == null
            ? feedback.getPriority()
            : normalizePriority(request.getPriority(), feedback.getType());

        if ("resolved".equals(nextStatus) && nextReply == null && previousReply == null) {
            throw new RuntimeException("Resolved feedback requires a reply");
        }

        feedback.setStatus(nextStatus);
        feedback.setPriority(nextPriority);
        feedback.setAdminReply(nextReply);
        feedback.setAdminId(adminId);
        feedback.setHandledAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.update(feedback);

        if (nextReply != null && !Objects.equals(previousReply, nextReply)) {
            messageService.sendSystemTaskMessage(
                adminId,
                feedback.getUserId(),
                null,
                String.format("[Feedback Reply] Your feedback \"%s\" has a new admin reply: %s", feedback.getTitle(), nextReply)
            );
        } else if (!Objects.equals(previousStatus, nextStatus)) {
            messageService.sendSystemTaskMessage(
                adminId,
                feedback.getUserId(),
                null,
                buildStatusUpdateMessage(feedback.getTitle(), nextStatus)
            );
        }

        return feedbackMapper.selectById(feedbackId);
    }

    private String buildStatusUpdateMessage(String title, String status) {
        if ("resolved".equals(status)) {
            return String.format("[Feedback Status] Your feedback \"%s\" has been resolved.", title);
        }
        if ("in_progress".equals(status)) {
            return String.format("[Feedback Status] Your feedback \"%s\" is now in progress.", title);
        }
        return String.format("[Feedback Status] Your feedback \"%s\" has been reopened.", title);
    }

    private void requireAdmin(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("无管理员权限");
        }
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase();
        if (
            "BUG".equals(normalized)
                || "SUGGESTION".equals(normalized)
                || "TASK_DISPUTE".equals(normalized)
                || "ACCOUNT_REPORT".equals(normalized)
                || "CONTENT_REPORT".equals(normalized)
                || "OTHER".equals(normalized)
        ) {
            return normalized;
        }
        return "OTHER";
    }

    private String normalizePriority(String priority, String type) {
        String normalized = priority == null ? "" : priority.trim().toUpperCase();
        if ("LOW".equals(normalized) || "NORMAL".equals(normalized) || "HIGH".equals(normalized) || "URGENT".equals(normalized)) {
            return normalized;
        }
        if ("ACCOUNT_REPORT".equals(type) || "CONTENT_REPORT".equals(type) || "TASK_DISPUTE".equals(type) || "BUG".equals(type)) {
            return "HIGH";
        }
        return "NORMAL";
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if ("open".equals(normalized) || "in_progress".equals(normalized) || "resolved".equals(normalized)) {
            return normalized;
        }
        throw new RuntimeException("反馈状态不合法");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
