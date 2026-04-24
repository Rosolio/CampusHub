package com.campushub.dto;

public class TaskCreateRequest {
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
    private String expiresAt;

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

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
