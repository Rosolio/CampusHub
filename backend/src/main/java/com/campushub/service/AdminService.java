package com.campushub.service;

import com.campushub.dto.AdminTaskReviewRequest;
import com.campushub.dto.AdminUserStatusUpdateRequest;
import com.campushub.dto.UserVO;
import com.campushub.entity.Task;
import com.campushub.entity.User;
import com.campushub.mapper.FeedbackMapper;
import com.campushub.mapper.TaskMapper;
import com.campushub.mapper.UserLoginLogMapper;
import com.campushub.mapper.UserMapper;
import com.campushub.mapper.UserVerificationMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminService {

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final UserVerificationMapper verificationMapper;
    private final FeedbackMapper feedbackMapper;
    private final UserService userService;
    private final MessageService messageService;
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminService(
        UserMapper userMapper,
        TaskMapper taskMapper,
        UserLoginLogMapper userLoginLogMapper,
        UserVerificationMapper verificationMapper,
        FeedbackMapper feedbackMapper,
        UserService userService,
        MessageService messageService,
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.userLoginLogMapper = userLoginLogMapper;
        this.verificationMapper = verificationMapper;
        this.feedbackMapper = feedbackMapper;
        this.userService = userService;
        this.messageService = messageService;
        this.redisTemplate = redisTemplate;
    }

    public void requireAdmin(Long userId) {
        User operator = userMapper.selectById(userId);
        if (operator == null || !"ADMIN".equalsIgnoreCase(operator.getRole())) {
            throw new RuntimeException("无管理员权限");
        }
    }

    public List<UserVO> getUsers(Long adminId) {
        requireAdmin(adminId);
        return userMapper.selectAdminUsers().stream().map(UserVO::from).toList();
    }

    @Transactional
    public UserVO updateUserStatus(Long adminId, Long userId, AdminUserStatusUpdateRequest request) {
        requireAdmin(adminId);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("管理员账号不支持禁用");
        }

        String nextStatus = normalizeUserStatus(request.getStatus());
        String disabledReason = "DISABLED".equals(nextStatus) ? trimToNull(request.getDisabledReason()) : null;
        userMapper.updateUserStatus(userId, nextStatus, disabledReason);
        redisTemplate.delete("users:" + userId);
        return UserVO.from(userMapper.selectById(userId));
    }

    public List<Task> getTasks(Long adminId) {
        requireAdmin(adminId);
        return taskMapper.selectAdminTasks();
    }

    @Transactional
    public Task reviewTask(Long adminId, Long taskId, AdminTaskReviewRequest request) {
        requireAdmin(adminId);
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("内容不存在");
        }

        String nextReviewStatus = normalizeReviewStatus(request.getReviewStatus());
        String reviewNote = trimToNull(request.getReviewNote());
        String previousReviewStatus = task.getReviewStatus();

        task.setReviewStatus(nextReviewStatus);
        task.setReviewNote(reviewNote);
        task.setReviewedBy(adminId);
        task.setReviewedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        if ("rejected".equals(nextReviewStatus) && !"rejected".equals(previousReviewStatus)) {
            userService.adjustScore(task.getRequesterId(), BigDecimal.valueOf(-1));
            String reasonSuffix = reviewNote == null ? "请修改后重新提交。" : "原因：" + reviewNote;
            messageService.sendSystemTaskMessage(
                adminId,
                task.getRequesterId(),
                task.getId(),
                String.format("【内容审核提醒】你发布的内容《%s》未通过审核，%s 本次已扣除 1.00 信用分。", task.getTitle(), reasonSuffix)
            );
        }

        Set<String> feedKeys = redisTemplate.keys("tasks:feed:*");
        if (feedKeys != null && !feedKeys.isEmpty()) {
            redisTemplate.delete(feedKeys);
        }
        redisTemplate.delete("tasks:" + taskId);
        return taskMapper.selectById(taskId);
    }

    public Map<String, Object> getDashboard(Long adminId) {
        requireAdmin(adminId);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("dailyActiveUsers", safeCount(userLoginLogMapper.countDistinctUsersBetween(today.atStartOfDay(), end)));
        overview.put("todayOrderCount", countValueForDate(taskMapper.countCompletedTasksByDateRange(today.atStartOfDay(), end), today));
        overview.put("totalUsers", userMapper.selectAdminUsers().size());
        overview.put("totalTasks", taskMapper.selectAdminTasks().size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("dailyActiveTrend", fillDailySeries(startDate, today, userLoginLogMapper.countDistinctUsersByDateRange(start, end)));
        result.put("orderTrend", fillDailySeries(startDate, today, taskMapper.countCompletedTasksByDateRange(start, end)));
        result.put("categoryDistribution", normalizeStatList(taskMapper.countTasksByCategory()));
        result.put("reviewDistribution", normalizeStatList(taskMapper.countTasksByReviewStatus()));
        result.put("userStatusDistribution", normalizeStatList(userMapper.countUsersByStatus()));
        result.put("verificationsPending", verificationMapper.countPendingVerifications());
        result.put("feedbackResolved", feedbackMapper.countByStatus("resolved"));
        result.put("feedbackTotal", feedbackMapper.countAll());
        int completedTasks = taskMapper.countCompletedTasksByDateRange(LocalDate.of(2020,1,1).atStartOfDay(), end).stream()
            .mapToInt(r -> ((Number) r.get("value")).intValue()).sum();
        result.put("taskCompletionRate", result.get("totalTasks") != null && ((Number) result.get("totalTasks")).intValue() > 0
            ? Math.round(completedTasks * 100.0 / ((Number) result.get("totalTasks")).intValue()) : 0);
        return result;
    }

    private String normalizeUserStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if ("ACTIVE".equals(normalized) || "DISABLED".equals(normalized)) {
            return normalized;
        }
        throw new RuntimeException("用户状态不合法");
    }

    private String normalizeReviewStatus(String reviewStatus) {
        String normalized = reviewStatus == null ? "" : reviewStatus.trim().toLowerCase();
        if ("approved".equals(normalized) || "rejected".equals(normalized) || "pending_review".equals(normalized)) {
            return normalized;
        }
        throw new RuntimeException("审核状态不合法");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private int countValueForDate(List<Map<String, Object>> rows, LocalDate date) {
        String target = date.toString();
        for (Map<String, Object> row : rows) {
            if (target.equals(String.valueOf(row.get("label")))) {
                return ((Number) row.get("value")).intValue();
            }
        }
        return 0;
    }

    private List<Map<String, Object>> fillDailySeries(LocalDate startDate, LocalDate endDate, List<Map<String, Object>> rows) {
        Map<String, Integer> values = new HashMap<>();
        for (Map<String, Object> row : rows) {
            values.put(String.valueOf(row.get("label")), ((Number) row.get("value")).intValue());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", cursor.toString());
            item.put("value", values.getOrDefault(cursor.toString(), 0));
            result.add(item);
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> batchUpdateUserStatus(Long adminId, Map<String, Object> body) {
        requireAdmin(adminId);
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("userIds");
        String status = (String) body.get("status");
        String disabledReason = (String) body.get("disabledReason");

        if (rawIds == null || rawIds.isEmpty()) throw new RuntimeException("userIds不能为空");
        if (status == null) throw new RuntimeException("status不能为空");
        String normalizedStatus = normalizeUserStatus(status);

        int processed = 0;
        for (Integer rawId : rawIds) {
            try {
                userMapper.updateUserStatus(rawId.longValue(), normalizedStatus, disabledReason);
                messageService.sendSystemTaskMessage(adminId, rawId.longValue(), null,
                    "你的账号状态已被管理员更新为：" + normalizedStatus);
                processed++;
            } catch (Exception ignored) {}
        }
        Map<String, Object> result = new HashMap<>();
        result.put("processed", processed);
        result.put("total", rawIds.size());
        return result;
    }

    @Transactional
    public Map<String, Object> batchReviewTasks(Long adminId, Map<String, Object> body) {
        requireAdmin(adminId);
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("taskIds");
        String reviewStatus = (String) body.get("reviewStatus");
        String reviewNote = (String) body.get("reviewNote");

        if (rawIds == null || rawIds.isEmpty()) throw new RuntimeException("taskIds不能为空");
        if (reviewStatus == null) throw new RuntimeException("reviewStatus不能为空");

        int processed = 0;
        for (Integer rawId : rawIds) {
            try {
                Task task = taskMapper.selectById(rawId.longValue());
                if (task != null) {
                    task.setReviewStatus(reviewStatus);
                    task.setReviewNote(reviewNote);
                    task.setReviewedBy(adminId);
                    task.setReviewedAt(LocalDateTime.now());
                    taskMapper.update(task);
                    messageService.sendSystemTaskMessage(adminId, task.getRequesterId(), task.getId(),
                        "你的帖子审核结果：" + reviewStatus);
                    processed++;
                }
            } catch (Exception ignored) {}
        }
        Map<String, Object> result = new HashMap<>();
        result.put("processed", processed);
        result.put("total", rawIds.size());
        return result;
    }

    private List<Map<String, Object>> normalizeStatList(List<Map<String, Object>> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", String.valueOf(row.get("label")));
            item.put("value", ((Number) row.get("value")).intValue());
            result.add(item);
        }
        return result;
    }
}
