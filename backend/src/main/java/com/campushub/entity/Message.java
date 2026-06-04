package com.campushub.entity;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long taskId;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private String senderName;
    private String senderAvatarUrl;
    private String receiverName;
    private String receiverAvatarUrl;
    private LocalDateTime senderLastLoginAt;
    private LocalDateTime receiverLastLoginAt;
    private Boolean senderOnline;
    private Boolean receiverOnline;
    private String taskTitle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverAvatarUrl() {
        return receiverAvatarUrl;
    }

    public void setReceiverAvatarUrl(String receiverAvatarUrl) {
        this.receiverAvatarUrl = receiverAvatarUrl;
    }

    public LocalDateTime getSenderLastLoginAt() {
        return senderLastLoginAt;
    }

    public void setSenderLastLoginAt(LocalDateTime senderLastLoginAt) {
        this.senderLastLoginAt = senderLastLoginAt;
    }

    public LocalDateTime getReceiverLastLoginAt() {
        return receiverLastLoginAt;
    }

    public void setReceiverLastLoginAt(LocalDateTime receiverLastLoginAt) {
        this.receiverLastLoginAt = receiverLastLoginAt;
    }

    public Boolean getSenderOnline() {
        return senderOnline;
    }

    public void setSenderOnline(Boolean senderOnline) {
        this.senderOnline = senderOnline;
    }

    public Boolean getReceiverOnline() {
        return receiverOnline;
    }

    public void setReceiverOnline(Boolean receiverOnline) {
        this.receiverOnline = receiverOnline;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }
}
