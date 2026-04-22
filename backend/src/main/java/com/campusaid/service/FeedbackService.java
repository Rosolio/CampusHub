package com.campusaid.service;

import com.campusaid.dto.AdminFeedbackUpdateRequest;
import com.campusaid.dto.FeedbackCreateRequest;
import com.campusaid.entity.Feedback;
import com.campusaid.mapper.FeedbackMapper;
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
        feedback.setType(normalizeType(request.getType()));
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setStatus("open");
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

        String previousReply = trimToNull(feedback.getAdminReply());
        String nextReply = trimToNull(request.getAdminReply());

        feedback.setStatus(normalizeStatus(request.getStatus()));
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
                String.format("【社区反馈回复】你提交的反馈《%s》有了新的管理员回复：%s", feedback.getTitle(), nextReply)
            );
        }

        return feedbackMapper.selectById(feedbackId);
    }

    private void requireAdmin(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("无管理员权限");
        }
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase();
        if ("BUG".equals(normalized) || "SUGGESTION".equals(normalized) || "OTHER".equals(normalized)) {
            return normalized;
        }
        return "OTHER";
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
