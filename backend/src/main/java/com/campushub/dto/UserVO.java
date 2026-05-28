package com.campushub.dto;

import com.campushub.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserVO {
    private Long id;
    private String studentId;
    private String name;
    private String email;
    private String avatarUrl;
    private String major;
    private String role;
    private String status;
    private String verifiedStatus;
    private BigDecimal score;
    private Integer points;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.id = user.getId();
        vo.studentId = user.getStudentId();
        vo.name = user.getName();
        vo.email = user.getEmail();
        vo.avatarUrl = user.getAvatarUrl();
        vo.major = user.getMajor();
        vo.role = user.getRole();
        vo.status = user.getStatus();
        vo.verifiedStatus = user.getVerifiedStatus();
        vo.score = user.getScore();
        vo.points = user.getPoints();
        vo.lastLoginAt = user.getLastLoginAt();
        vo.createdAt = user.getCreatedAt();
        return vo;
    }

    public Long getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getMajor() {
        return major;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getVerifiedStatus() {
        return verifiedStatus;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Integer getPoints() {
        return points;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
