package com.campushub.entity;

import java.time.LocalDateTime;

public class TaskComment {
    private Long id;
    private Long taskId;
    private Long authorId;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private String authorAvatarUrl;
    private String authorMajor;
    private String replyToAuthorName;
    private Integer likeCount;
    private Boolean likedByCurrentUser;

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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getAuthorMajor() {
        return authorMajor;
    }

    public void setAuthorMajor(String authorMajor) {
        this.authorMajor = authorMajor;
    }

    public String getReplyToAuthorName() {
        return replyToAuthorName;
    }

    public void setReplyToAuthorName(String replyToAuthorName) {
        this.replyToAuthorName = replyToAuthorName;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
