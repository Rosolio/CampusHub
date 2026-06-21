package com.campushub.service;

import com.campushub.dto.TaskReviewCreateRequest;
import com.campushub.entity.Task;
import com.campushub.entity.TaskParticipant;
import com.campushub.entity.TaskReview;
import com.campushub.entity.User;
import com.campushub.mapper.TaskMapper;
import com.campushub.mapper.TaskParticipantMapper;
import com.campushub.mapper.TaskReviewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskReviewService {
    private static final int REVIEW_SUBMISSION_REWARD_POINTS = 3;

    private final TaskMapper taskMapper;
    private final TaskParticipantMapper taskParticipantMapper;
    private final TaskReviewMapper taskReviewMapper;
    private final UserService userService;
    private final MessageService messageService;
    private final NotificationService notificationService;

    public TaskReviewService(
        TaskMapper taskMapper,
        TaskParticipantMapper taskParticipantMapper,
        TaskReviewMapper taskReviewMapper,
        UserService userService,
        MessageService messageService,
        NotificationService notificationService
    ) {
        this.taskMapper = taskMapper;
        this.taskParticipantMapper = taskParticipantMapper;
        this.taskReviewMapper = taskReviewMapper;
        this.userService = userService;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    public List<TaskReview> getTaskReviews(Long taskId) {
        ensureReviewableTask(taskId);
        return taskReviewMapper.selectByTaskId(taskId);
    }

    public Map<Long, Integer> getTaskReviewCounts(List<Long> taskIds) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        if (taskIds == null || taskIds.isEmpty()) {
            return result;
        }

        for (Long taskId : taskIds) {
            ensureReviewableTask(taskId);
            result.put(taskId, 0);
        }

        for (Map<String, Object> row : taskReviewMapper.countByTaskIds(taskIds)) {
            Long taskId = ((Number) row.get("taskId")).longValue();
            int reviewCount = ((Number) row.get("reviewCount")).intValue();
            result.put(taskId, reviewCount);
        }

        return result;
    }

    @Transactional
    public TaskReview createTaskReview(Long taskId, TaskReviewCreateRequest request, Long reviewerId) {
        Task task = ensureReviewableTask(taskId);
        if (!"completed".equals(task.getStatus())) {
            throw new RuntimeException("任务尚未完成，暂时不能评价");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("评分必须在 1 到 5 星之间");
        }
        if (taskReviewMapper.selectByTaskIdAndReviewerId(taskId, reviewerId) != null) {
            throw new RuntimeException("你已经评价过这项任务");
        }

        TaskParticipant helperParticipant = taskParticipantMapper.selectByTaskId(taskId).stream()
            .filter(participant -> "helper".equals(participant.getRole()) && !"canceled".equals(participant.getStatus()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("当前任务缺少有效的接单人"));

        boolean isRequester = reviewerId.equals(task.getRequesterId());
        boolean isHelper = reviewerId.equals(helperParticipant.getParticipantId());
        if (!isRequester && !isHelper) {
            throw new RuntimeException("只有任务双方可以评价");
        }

        TaskReview review = new TaskReview();
        review.setTaskId(taskId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(isRequester ? helperParticipant.getParticipantId() : task.getRequesterId());
        review.setReviewerRole(isRequester ? "requester" : "helper");
        review.setRating(request.getRating());
        review.setContent(normalizeContent(request.getContent()));
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        taskReviewMapper.insert(review);

        BigDecimal averageRating = taskReviewMapper.selectAverageRatingByRevieweeId(review.getRevieweeId());
        userService.updateScore(review.getRevieweeId(), averageRating);
        userService.addPoints(reviewerId, REVIEW_SUBMISSION_REWARD_POINTS, "TASK_REVIEW_REWARD", "提交互评奖励", "task_review", review.getId());
        userService.addPoints(
            review.getRevieweeId(),
            resolveRewardPointsByRating(request.getRating()),
            "TASK_REVIEW_RESULT",
            "互评结算积分",
            "task_review",
            review.getId()
        );

        User reviewer = userService.getUserById(reviewerId);
        if (reviewer != null && !reviewerId.equals(review.getRevieweeId())) {
            String reviewerName = reviewer.getName() != null && !reviewer.getName().isBlank()
                ? reviewer.getName()
                : "对方";
            messageService.sendSystemTaskMessage(
                reviewerId,
                review.getRevieweeId(),
                taskId,
                String.format(
                    "【评价通知】%s 已提交需求《%s》的互助评价，评分为 %d 星。你本次将结算 %d 积分，信用分也已同步更新。",
                    reviewerName,
                    task.getTitle(),
                    request.getRating(),
                    resolveRewardPointsByRating(request.getRating())
                )
            );
            notificationService.createNotification(
                review.getRevieweeId(),
                "REVIEW_RECEIVED",
                "收到互助评价",
                String.format("%s 对需求《%s》给出了 %d 星评价", reviewerName, task.getTitle(), request.getRating()),
                "task",
                taskId
            );
        }

        return taskReviewMapper.selectById(review.getId());
    }

    private Task ensureReviewableTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"task".equals(task.getTaskMode())) {
            throw new RuntimeException("只有互助任务支持评价");
        }
        return task;
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }
        String normalized = content.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private int resolveRewardPointsByRating(Integer rating) {
        if (rating == null) {
            return 0;
        }
        switch (rating) {
            case 5:
                return 8;
            case 4:
                return 5;
            case 3:
                return 0;
            case 2:
                return -2;
            case 1:
                return -3;
            default:
                return 0;
        }
    }
}
