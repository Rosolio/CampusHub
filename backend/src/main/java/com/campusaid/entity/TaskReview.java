package com.campusaid.entity;

import java.time.LocalDateTime;

public class TaskReview {
    private Long id;
    private Long taskId;
    private Long reviewerId;
    private Long revieweeId;
    private String reviewerRole;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reviewerName;
    private String reviewerAvatarUrl;
    private String revieweeName;
    private String revieweeAvatarUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getReviewerRole() {
        return reviewerRole;
    }

    public void setReviewerRole(String reviewerRole) {
        this.reviewerRole = reviewerRole;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerAvatarUrl() {
        return reviewerAvatarUrl;
    }

    public void setReviewerAvatarUrl(String reviewerAvatarUrl) {
        this.reviewerAvatarUrl = reviewerAvatarUrl;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public String getRevieweeAvatarUrl() {
        return revieweeAvatarUrl;
    }

    public void setRevieweeAvatarUrl(String revieweeAvatarUrl) {
        this.revieweeAvatarUrl = revieweeAvatarUrl;
    }
}
