package com.campusaid.service;

import com.campusaid.entity.User;
import com.campusaid.entity.UserPointRecord;
import com.campusaid.entity.UserSetting;
import com.campusaid.mapper.UserPointRecordMapper;
import com.campusaid.mapper.UserMapper;
import com.campusaid.mapper.UserSettingMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserPointRecordMapper userPointRecordMapper;
    private final UserSettingMapper userSettingMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(
        UserMapper userMapper,
        UserPointRecordMapper userPointRecordMapper,
        UserSettingMapper userSettingMapper,
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.userMapper = userMapper;
        this.userPointRecordMapper = userPointRecordMapper;
        this.userSettingMapper = userSettingMapper;
        this.redisTemplate = redisTemplate;
    }

    public User getUserById(Long id) {
        String key = "users:" + id;
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user == null) {
            user = userMapper.selectById(id);
            if (user != null) {
                redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
            }
        }
        return user;
    }

    public boolean isAdmin(Long id) {
        if (id == null) {
            return false;
        }
        User user = getUserById(id);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    public User updateUser(Long id, User user) {
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getMajor() != null) {
            existingUser.setMajor(user.getMajor());
        }
        existingUser.setUpdatedAt(LocalDateTime.now());
        userMapper.update(existingUser);

        // 清除缓存
        redisTemplate.delete("users:" + id);

        return existingUser;
    }

    public UserSetting getUserSetting(Long userId) {
        String key = "user_settings:" + userId;
        UserSetting userSetting = (UserSetting) redisTemplate.opsForValue().get(key);
        if (userSetting == null) {
            userSetting = userSettingMapper.selectByUserId(userId);
            if (userSetting == null) {
                // 创建默认设置
                userSetting = new UserSetting();
                userSetting.setUserId(userId);
                userSetting.setNotificationEnabled(true);
                userSetting.setTheme("light");
                userSetting.setLanguage("zh-CN");
                userSetting.setUpdatedAt(LocalDateTime.now());
                userSettingMapper.insert(userSetting);
            }
            redisTemplate.opsForValue().set(key, userSetting, 30, TimeUnit.MINUTES);
        }
        return userSetting;
    }

    public UserSetting updateUserSetting(Long userId, UserSetting userSetting) {
        UserSetting existingSetting = userSettingMapper.selectByUserId(userId);
        if (existingSetting == null) {
            userSetting.setUserId(userId);
            userSetting.setUpdatedAt(LocalDateTime.now());
            userSettingMapper.insert(userSetting);
        } else {
            existingSetting.setNotificationEnabled(userSetting.getNotificationEnabled());
            existingSetting.setTheme(userSetting.getTheme());
            existingSetting.setLanguage(userSetting.getLanguage());
            existingSetting.setUpdatedAt(LocalDateTime.now());
            userSettingMapper.update(existingSetting);
            userSetting = existingSetting;
        }

        // 清除缓存
        redisTemplate.delete("user_settings:" + userId);

        return userSetting;
    }

    public void addPoints(Long userId, int points) {
        addPoints(userId, points, "GENERAL", "积分变动", null, null);
    }

    public void addPoints(Long userId, int points, String changeType, String description, String referenceType, Long referenceId) {
        userMapper.incrementPoints(userId, points);
        UserPointRecord record = new UserPointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setChangeType(changeType);
        record.setDescription(description);
        record.setReferenceType(referenceType);
        record.setReferenceId(referenceId);
        record.setCreatedAt(LocalDateTime.now());
        userPointRecordMapper.insert(record);
        redisTemplate.delete("users:" + userId);
    }

    public List<UserPointRecord> getPointRecords(Long userId) {
        return userPointRecordMapper.selectByUserId(userId, 50);
    }

    public int getTodayCommentRewardPoints(Long userId) {
        LocalDate today = LocalDate.now();
        Integer points = userPointRecordMapper.sumPositivePointsByUserIdAndChangeTypeBetween(
            userId,
            "COMMENT_REWARD",
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );
        return points == null ? 0 : points;
    }

    public void updateScore(Long userId, BigDecimal score) {
        BigDecimal normalizedScore = score == null
            ? BigDecimal.ZERO
            : score.setScale(2, RoundingMode.HALF_UP);
        userMapper.updateScore(userId, normalizedScore);
        redisTemplate.delete("users:" + userId);
    }

}
