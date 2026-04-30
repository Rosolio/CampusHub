package com.campushub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Task {
    private Long id;
    private Long requesterId;
    private String title;
    private String description;
    private String category;
    private String taskMode;
    private String badgePrimary;
    private String badgeSecondary;
    private String locationText;
    private String timeText;
    private String rewardTitle;
    private String rewardText;
    private String impactTitle;
    private String impactText;
    private String mapImageUrl;
    private String contactInfo;
    private String reviewStatus;
    private String reviewNote;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String requesterName;
    private String requesterMajor;
    private String requesterAvatarUrl;
    private BigDecimal requesterScore;
    private Integer requesterPoints;
    private Long helperId;
    private String helperName;
    private String helperMajor;
    private String helperAvatarUrl;
    private BigDecimal helperScore;
    private Integer helperPoints;
    private String status;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean likedByCurrentUser;
    private LocalDateTime expiresAt;
    private LocalDateTime requesterCompletedAt;
    private LocalDateTime helperCompletedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTaskMode() {
        return taskMode;
    }

    public void setTaskMode(String taskMode) {
        this.taskMode = taskMode;
    }

    public String getBadgePrimary() {
        return badgePrimary;
    }

    public void setBadgePrimary(String badgePrimary) {
        this.badgePrimary = badgePrimary;
    }

    public String getBadgeSecondary() {
        return badgeSecondary;
    }

    public void setBadgeSecondary(String badgeSecondary) {
        this.badgeSecondary = badgeSecondary;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public String getRewardText() {
        return rewardText;
    }

    public void setRewardText(String rewardText) {
        this.rewardText = rewardText;
    }

    public String getImpactTitle() {
        return impactTitle;
    }

    public void setImpactTitle(String impactTitle) {
        this.impactTitle = impactTitle;
    }

    public String getImpactText() {
        return impactText;
    }

    public void setImpactText(String impactText) {
        this.impactText = impactText;
    }

    public String getMapImageUrl() {
        return mapImageUrl;
    }

    public void setMapImageUrl(String mapImageUrl) {
        this.mapImageUrl = mapImageUrl;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterMajor() {
        return requesterMajor;
    }

    public void setRequesterMajor(String requesterMajor) {
        this.requesterMajor = requesterMajor;
    }

    public String getRequesterAvatarUrl() {
        return requesterAvatarUrl;
    }

    public void setRequesterAvatarUrl(String requesterAvatarUrl) {
        this.requesterAvatarUrl = requesterAvatarUrl;
    }

    public BigDecimal getRequesterScore() {
        return requesterScore;
    }

    public void setRequesterScore(BigDecimal requesterScore) {
        this.requesterScore = requesterScore;
    }

    public Integer getRequesterPoints() {
        return requesterPoints;
    }

    public void setRequesterPoints(Integer requesterPoints) {
        this.requesterPoints = requesterPoints;
    }

    public Long getHelperId() {
        return helperId;
    }

    public void setHelperId(Long helperId) {
        this.helperId = helperId;
    }

    public String getHelperName() {
        return helperName;
    }

    public void setHelperName(String helperName) {
        this.helperName = helperName;
    }

    public String getHelperMajor() {
        return helperMajor;
    }

    public void setHelperMajor(String helperMajor) {
        this.helperMajor = helperMajor;
    }

    public String getHelperAvatarUrl() {
        return helperAvatarUrl;
    }

    public void setHelperAvatarUrl(String helperAvatarUrl) {
        this.helperAvatarUrl = helperAvatarUrl;
    }

    public BigDecimal getHelperScore() {
        return helperScore;
    }

    public void setHelperScore(BigDecimal helperScore) {
        this.helperScore = helperScore;
    }

    public Integer getHelperPoints() {
        return helperPoints;
    }

    public void setHelperPoints(Integer helperPoints) {
        this.helperPoints = helperPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRequesterCompletedAt() {
        return requesterCompletedAt;
    }

    public void setRequesterCompletedAt(LocalDateTime requesterCompletedAt) {
        this.requesterCompletedAt = requesterCompletedAt;
    }

    public LocalDateTime getHelperCompletedAt() {
        return helperCompletedAt;
    }

    public void setHelperCompletedAt(LocalDateTime helperCompletedAt) {
        this.helperCompletedAt = helperCompletedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
}
