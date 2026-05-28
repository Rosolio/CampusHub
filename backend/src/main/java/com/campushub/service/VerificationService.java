package com.campushub.service;

import com.campushub.entity.UserVerification;
import com.campushub.mapper.UserMapper;
import com.campushub.mapper.UserVerificationMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final UserVerificationMapper verificationMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final RedisTemplate<String, Object> redisTemplate;

    public VerificationService(UserVerificationMapper verificationMapper, UserMapper userMapper,
                               MessageService messageService, RedisTemplate<String, Object> redisTemplate) {
        this.verificationMapper = verificationMapper;
        this.userMapper = userMapper;
        this.messageService = messageService;
        this.redisTemplate = redisTemplate;
    }

    private void clearUserCache(Long userId) {
        redisTemplate.delete("users:" + userId);
    }

    @Transactional
    public UserVerification submit(Long userId, String realName, String studentId, List<String> imageUrls) {
        var user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("VERIFIED".equals(user.getVerifiedStatus())) {
            throw new RuntimeException("您已通过认证，无需重复申请");
        }
        if ("PENDING".equals(user.getVerifiedStatus())) {
            throw new RuntimeException("您的认证申请正在审核中，请耐心等待");
        }

        String imageUrlsJson = "[" + String.join(",", imageUrls.stream().map(url -> "\"" + url + "\"").toList()) + "]";

        var existing = verificationMapper.selectByUserId(userId);
        if (existing != null) {
            existing.setRealName(realName);
            existing.setStudentId(studentId);
            existing.setImageUrls(imageUrlsJson);
            existing.setStatus("PENDING");
            existing.setRejectReason(null);
            existing.setReviewerId(null);
            existing.setReviewedAt(null);
            existing.setUpdatedAt(LocalDateTime.now());
            verificationMapper.update(existing);
        } else {
            var verification = new UserVerification();
            verification.setUserId(userId);
            verification.setType("STUDENT");
            verification.setRealName(realName);
            verification.setStudentId(studentId);
            verification.setImageUrls(imageUrlsJson);
            verification.setStatus("PENDING");
            verification.setCreatedAt(LocalDateTime.now());
            verification.setUpdatedAt(LocalDateTime.now());
            verificationMapper.insert(verification);
        }

        userMapper.updateVerifiedStatus(userId, "PENDING");
        clearUserCache(userId);

        messageService.sendSystemTaskMessage(
            4L, userId, null,
            "您的校园认证申请已提交，请耐心等待管理员审核。"
        );

        return verificationMapper.selectByUserId(userId);
    }

    public UserVerification getMyVerification(Long userId) {
        return verificationMapper.selectByUserId(userId);
    }

    public List<UserVerification> getAllVerifications() {
        return verificationMapper.selectAll();
    }

    @Transactional
    public UserVerification review(Long reviewerId, Long verificationId, String status, String rejectReason) {
        var verification = verificationMapper.selectById(verificationId);
        if (verification == null) {
            throw new RuntimeException("认证申请不存在");
        }
        if (!"PENDING".equals(verification.getStatus())) {
            throw new RuntimeException("该申请已处理，无需重复审核");
        }

        verification.setStatus(status);
        verification.setReviewerId(reviewerId);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());

        if ("APPROVED".equals(status)) {
            verification.setRejectReason(null);
            userMapper.updateVerifiedStatus(verification.getUserId(), "VERIFIED");
            clearUserCache(verification.getUserId());
            messageService.sendSystemTaskMessage(
                4L, verification.getUserId(), null,
                "恭喜，您的校园认证已通过！"
            );
        } else if ("REJECTED".equals(status)) {
            verification.setRejectReason(rejectReason);
            userMapper.updateVerifiedStatus(verification.getUserId(), "NONE");
            clearUserCache(verification.getUserId());
            messageService.sendSystemTaskMessage(
                4L, verification.getUserId(), null,
                "您的校园认证未通过，原因：" + (rejectReason != null ? rejectReason : "资料不符合要求") + "。请重新上传资料。"
            );
        } else {
            throw new RuntimeException("无效的审核状态");
        }

        verificationMapper.update(verification);
        return verificationMapper.selectById(verificationId);
    }

    @Transactional
    public UserVerification revoke(Long reviewerId, Long verificationId) {
        var verification = verificationMapper.selectById(verificationId);
        if (verification == null) {
            throw new RuntimeException("认证申请不存在");
        }
        if (!"APPROVED".equals(verification.getStatus())) {
            throw new RuntimeException("只能撤销已通过的认证");
        }

        verification.setStatus("REVOKED");
        verification.setReviewerId(reviewerId);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        verificationMapper.update(verification);

        userMapper.updateVerifiedStatus(verification.getUserId(), "NONE");
        clearUserCache(verification.getUserId());

        messageService.sendSystemTaskMessage(
            4L, verification.getUserId(), null,
            "您的校园认证已被管理员撤销，如有疑问请联系管理员。"
        );

        return verificationMapper.selectById(verificationId);
    }
}
