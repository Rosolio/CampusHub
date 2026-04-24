package com.campushub.service;

import com.campushub.dto.TaskCreateRequest;
import com.campushub.entity.Task;
import com.campushub.entity.TaskLike;
import com.campushub.entity.TaskParticipant;
import com.campushub.mapper.TaskCommentMapper;
import com.campushub.mapper.TaskCommentLikeMapper;
import com.campushub.mapper.TaskLikeMapper;
import com.campushub.mapper.TaskMapper;
import com.campushub.mapper.TaskParticipantMapper;
import com.campushub.mapper.TaskReviewMapper;
import com.campushub.util.TaskModeResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Set<String> TOPIC_CATEGORIES = Set.of("二手闲置", "恋爱交友", "打听求助", "兼职招聘");
    private static final Set<String> TASK_CATEGORIES = Set.of("跑腿代办", "学习辅导");

    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;
    private final TaskLikeMapper taskLikeMapper;
    private final TaskCommentLikeMapper taskCommentLikeMapper;
    private final TaskParticipantMapper taskParticipantMapper;
    private final TaskReviewMapper taskReviewMapper;
    private final UserService userService;
    private final MessageService messageService;
    private final RedisTemplate<String, Object> redisTemplate;

    public TaskService(
        TaskMapper taskMapper,
        TaskCommentMapper taskCommentMapper,
        TaskLikeMapper taskLikeMapper,
        TaskCommentLikeMapper taskCommentLikeMapper,
        TaskParticipantMapper taskParticipantMapper,
        TaskReviewMapper taskReviewMapper,
        UserService userService,
        MessageService messageService,
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.taskMapper = taskMapper;
        this.taskCommentMapper = taskCommentMapper;
        this.taskLikeMapper = taskLikeMapper;
        this.taskCommentLikeMapper = taskCommentLikeMapper;
        this.taskParticipantMapper = taskParticipantMapper;
        this.taskReviewMapper = taskReviewMapper;
        this.userService = userService;
        this.messageService = messageService;
        this.redisTemplate = redisTemplate;
    }

    public List<Task> getTasks() {
        return getTasks(null);
    }

    public List<Task> getTasks(Long currentUserId) {
        String key = "tasks:all";
        List<Task> tasks = (List<Task>) redisTemplate.opsForValue().get(key);
        if (tasks == null) {
            tasks = taskMapper.selectAll();
            redisTemplate.opsForValue().set(key, tasks, 5, TimeUnit.MINUTES);
        }
        return tasks.stream()
            .filter(this::isVisibleInCommunityFeed)
            .map(TaskModeResolver::normalize)
            .peek(task -> task.setLikedByCurrentUser(isTaskLikedByUser(task, currentUserId)))
            .collect(Collectors.toList());
    }

    public Task getTaskById(Long id) {
        return getTaskById(id, null);
    }

    public Task getTaskById(Long id, Long currentUserId) {
        String key = "tasks:" + id;
        Task task = (Task) redisTemplate.opsForValue().get(key);
        if (task == null) {
            task = taskMapper.selectById(id);
            if (task != null) {
                redisTemplate.opsForValue().set(key, task, 10, TimeUnit.MINUTES);
            }
        }
        if (task != null) {
            TaskModeResolver.normalize(task);
            if (!isVisibleToUser(task, currentUserId)) {
                throw new RuntimeException("帖子已截止，仅发布者自己可见");
            }
            task.setLikedByCurrentUser(isTaskLikedByUser(task, currentUserId));
        }
        return task;
    }

    @Transactional
    public Task createTask(TaskCreateRequest request, Long requesterId) {
        requireCommunityUser(requesterId);
        Task task = new Task();
        task.setRequesterId(requesterId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCategory(resolveCategory(request));
        task.setTaskMode(resolveTaskMode(request));
        task.setBadgePrimary(request.getBadgePrimary());
        task.setBadgeSecondary(request.getBadgeSecondary());
        task.setLocationText(request.getLocationText());
        task.setTimeText(request.getTimeText());
        task.setRewardTitle(request.getRewardTitle());
        task.setRewardText(request.getRewardText());
        task.setImpactTitle(request.getImpactTitle());
        task.setImpactText(request.getImpactText());
        task.setMapImageUrl(request.getMapImageUrl());
        task.setContactInfo(request.getContactInfo());
        task.setReviewStatus("approved");
        task.setReviewNote(null);
        task.setReviewedBy(null);
        task.setReviewedAt(null);
        task.setStatus("pending");
        task.setLikeCount(0);
        task.setExpiresAt(resolveExpiresAt(request));
        task.setRequesterCompletedAt(null);
        task.setHelperCompletedAt(null);
        task.setCompletedAt(null);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);

        // 创建任务参与者记录
        TaskParticipant participant = new TaskParticipant();
        participant.setTaskId(task.getId());
        participant.setParticipantId(requesterId);
        participant.setRole("requester");
        participant.setStatus("accepted");
        participant.setCreatedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());
        taskParticipantMapper.insert(participant);

        // 清除缓存
        redisTemplate.delete("tasks:all");

        return TaskModeResolver.normalize(task);
    }

    @Transactional
    public Task acceptTask(Long taskId, Long helperId) {
        requireCommunityUser(helperId);
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"task".equals(task.getTaskMode())) {
            throw new RuntimeException("该内容为话题帖，不支持接单");
        }
        if (helperId.equals(task.getRequesterId())) {
            throw new RuntimeException("不能接自己发布的任务");
        }
        if (!"pending".equals(task.getStatus())) {
            throw new RuntimeException("任务已被接受或完成");
        }

        task.setStatus("accepted");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        TaskParticipant participant = taskParticipantMapper.selectByTaskIdAndParticipantIdAndRole(taskId, helperId, "helper");
        if (participant == null) {
            participant = new TaskParticipant();
            participant.setTaskId(taskId);
            participant.setParticipantId(helperId);
            participant.setRole("helper");
            participant.setStatus("accepted");
            participant.setCreatedAt(LocalDateTime.now());
            participant.setUpdatedAt(LocalDateTime.now());
            taskParticipantMapper.insert(participant);
        } else {
            participant.setStatus("accepted");
            participant.setUpdatedAt(LocalDateTime.now());
            taskParticipantMapper.update(participant);
        }

        // 清除缓存
        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);

        notifyTaskEvent(
            helperId,
            task.getRequesterId(),
            taskId,
            String.format("【接单通知】你发布的需求《%s》已有同学接单，可以继续沟通具体细节。", task.getTitle())
        );

        return TaskModeResolver.normalize(task);
    }

    @Transactional
    public Task unacceptTask(Long taskId, Long helperId) {
        requireCommunityUser(helperId);
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"task".equals(task.getTaskMode())) {
            throw new RuntimeException("话题帖不支持取消接单");
        }
        if (!"accepted".equals(task.getStatus())) {
            throw new RuntimeException("当前任务未处于接单中状态");
        }

        TaskParticipant participant = taskParticipantMapper.selectByTaskIdAndParticipantIdAndRole(taskId, helperId, "helper");
        if (participant == null || !"accepted".equals(participant.getStatus())) {
            throw new RuntimeException("你当前没有接下这项任务");
        }

        participant.setStatus("canceled");
        participant.setUpdatedAt(LocalDateTime.now());
        taskParticipantMapper.update(participant);

        task.setStatus("pending");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);

        return TaskModeResolver.normalize(task);
    }

    @Transactional
    public Task completeTask(Long taskId, Long requesterId) {
        requireCommunityUser(requesterId);
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"task".equals(task.getTaskMode())) {
            throw new RuntimeException("话题帖不支持完成操作");
        }
        if (!"accepted".equals(task.getStatus()) && !"completion_pending".equals(task.getStatus())) {
            throw new RuntimeException("当前任务暂不支持确认完成");
        }

        List<TaskParticipant> participants = taskParticipantMapper.selectByTaskId(taskId);
        TaskParticipant helperParticipant = participants.stream()
            .filter(participant -> "helper".equals(participant.getRole()) && !"canceled".equals(participant.getStatus()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("当前任务缺少有效的接单人"));

        TaskParticipant requesterParticipant = participants.stream()
            .filter(participant -> "requester".equals(participant.getRole()))
            .findFirst()
            .orElse(null);

        boolean isRequester = requesterId.equals(task.getRequesterId());
        boolean isHelper = requesterId.equals(helperParticipant.getParticipantId());
        if (!isRequester && !isHelper) {
            throw new RuntimeException("只有任务双方可以确认完成");
        }

        LocalDateTime now = LocalDateTime.now();
        if (isRequester) {
            if (task.getRequesterCompletedAt() != null) {
                throw new RuntimeException("你已经确认过完成");
            }
            task.setRequesterCompletedAt(now);
            if (requesterParticipant != null) {
                requesterParticipant.setStatus("completion_pending");
                requesterParticipant.setUpdatedAt(now);
                taskParticipantMapper.update(requesterParticipant);
            }
        } else {
            if (task.getHelperCompletedAt() != null) {
                throw new RuntimeException("你已经确认过完成");
            }
            task.setHelperCompletedAt(now);
            helperParticipant.setStatus("completion_pending");
            helperParticipant.setUpdatedAt(now);
            taskParticipantMapper.update(helperParticipant);
        }

        if (task.getRequesterCompletedAt() != null && task.getHelperCompletedAt() != null) {
            task.setStatus("completed");
            task.setCompletedAt(now);
            task.setUpdatedAt(now);
            taskMapper.update(task);

            if (requesterParticipant != null) {
                requesterParticipant.setStatus("completed");
                requesterParticipant.setUpdatedAt(now);
                taskParticipantMapper.update(requesterParticipant);
            }
            helperParticipant.setStatus("completed");
            helperParticipant.setUpdatedAt(now);
            taskParticipantMapper.update(helperParticipant);

            userService.addPoints(task.getRequesterId(), 10, "TASK_COMPLETION_REWARD", "完成任务奖励", "task", taskId);
            userService.addPoints(helperParticipant.getParticipantId(), 10, "TASK_COMPLETION_REWARD", "完成任务奖励", "task", taskId);
            notifyTaskEvent(
                requesterId,
                isRequester ? helperParticipant.getParticipantId() : task.getRequesterId(),
                taskId,
                String.format("【等待互评】需求《%s》已由双方确认完成，请尽快进入互评页面完成评价。", task.getTitle())
            );
        } else {
            task.setStatus("completion_pending");
            task.setUpdatedAt(now);
            taskMapper.update(task);
            notifyTaskEvent(
                requesterId,
                isRequester ? helperParticipant.getParticipantId() : task.getRequesterId(),
                taskId,
                String.format("【完成确认】需求《%s》已由对方确认完成，等待你确认后即可进入互评。", task.getTitle())
            );
        }

        // 清除缓存
        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);

        return TaskModeResolver.normalize(task);
    }

    @Transactional
    public Task cancelTask(Long taskId, Long requesterId) {
        requireCommunityUser(requesterId);
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!requesterId.equals(task.getRequesterId())) {
            throw new RuntimeException("无权取消该任务");
        }
        if ("completed".equals(task.getStatus())) {
            throw new RuntimeException("任务已完成，无法取消");
        }

        task.setStatus("canceled");
        task.setRequesterCompletedAt(null);
        task.setHelperCompletedAt(null);
        task.setCompletedAt(null);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        taskParticipantMapper.selectByTaskId(taskId).forEach(participant -> {
            participant.setStatus("canceled");
            participant.setUpdatedAt(LocalDateTime.now());
            taskParticipantMapper.update(participant);
        });

        // 清除缓存
        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);

        return TaskModeResolver.normalize(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Long requesterId) {
        requireCommunityUser(requesterId);
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!requesterId.equals(task.getRequesterId())) {
            throw new RuntimeException("无权删除该任务");
        }
        if ("accepted".equals(task.getStatus()) || "completed".equals(task.getStatus())) {
            throw new RuntimeException("进行中或已完成的任务无法删除");
        }

        taskLikeMapper.deleteByTaskId(taskId);
        taskCommentLikeMapper.deleteByTaskId(taskId);
        taskCommentMapper.deleteByTaskId(taskId);
        taskReviewMapper.deleteByTaskId(taskId);
        taskParticipantMapper.deleteByTaskId(taskId);
        taskMapper.delete(taskId);

        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);
    }

    public List<Task> getMyTasks(Long userId) {
        if (userService.isAdmin(userId)) {
            return List.of();
        }
        return taskMapper.selectByRequesterId(userId).stream()
            .map(TaskModeResolver::normalize)
            .collect(Collectors.toList());
    }

    public List<Task> getMyAcceptedTasks(Long userId) {
        if (userService.isAdmin(userId)) {
            return List.of();
        }
        return taskMapper.selectByHelperId(userId).stream()
            .map(TaskModeResolver::normalize)
            .collect(Collectors.toList());
    }

    @Transactional
    public Task likeTask(Long taskId, Long userId) {
        Task task = TaskModeResolver.normalize(taskMapper.selectById(taskId));
        validateLikeableTopicTask(task);
        if (userId.equals(task.getRequesterId())) {
            throw new RuntimeException("不能给自己发布的帖子点赞");
        }
        if (taskLikeMapper.selectByTaskIdAndUserId(taskId, userId) != null) {
            throw new RuntimeException("你已经点过赞了");
        }

        TaskLike taskLike = new TaskLike();
        taskLike.setTaskId(taskId);
        taskLike.setUserId(userId);
        taskLike.setCreatedAt(LocalDateTime.now());
        taskLikeMapper.insert(taskLike);

        task.setLikeCount((task.getLikeCount() == null ? 0 : task.getLikeCount()) + 1);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);
        userService.addPoints(task.getRequesterId(), 1, "TOPIC_LIKE_REWARD", "帖子获赞奖励", "task", taskId);

        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);

        task.setLikedByCurrentUser(true);
        return task;
    }

    @Transactional
    public Task unlikeTask(Long taskId, Long userId) {
        Task task = TaskModeResolver.normalize(taskMapper.selectById(taskId));
        validateLikeableTopicTask(task);
        if (taskLikeMapper.selectByTaskIdAndUserId(taskId, userId) == null) {
            throw new RuntimeException("你还没有点过赞");
        }

        taskLikeMapper.deleteByTaskIdAndUserId(taskId, userId);

        int nextLikeCount = Math.max(0, (task.getLikeCount() == null ? 0 : task.getLikeCount()) - 1);
        task.setLikeCount(nextLikeCount);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);
        userService.addPoints(task.getRequesterId(), -1, "TOPIC_LIKE_REVOKE", "帖子取消点赞扣回", "task", taskId);

        redisTemplate.delete("tasks:all");
        redisTemplate.delete("tasks:" + taskId);

        task.setLikedByCurrentUser(false);
        return task;
    }

    public int getMyReceivedLikeCount(Long userId) {
        if (userService.isAdmin(userId)) {
            return 0;
        }
        Integer taskLikes = taskMapper.sumTopicLikesByRequesterId(userId);
        Integer commentLikes = taskCommentMapper.sumLikesByAuthorId(userId);
        return (taskLikes == null ? 0 : taskLikes) + (commentLikes == null ? 0 : commentLikes);
    }

    private void requireCommunityUser(Long userId) {
        if (userService.isAdmin(userId)) {
            throw new RuntimeException("管理员账号不参与普通社区发布、接单和履约流程");
        }
    }

    private void validateLikeableTopicTask(Task task) {
        Task normalizedTask = TaskModeResolver.normalize(task);
        if (normalizedTask == null) {
            throw new RuntimeException("帖子不存在");
        }
        if (!TaskModeResolver.isTopicTask(normalizedTask)) {
            throw new RuntimeException("当前内容不是话题帖，暂不支持点赞");
        }
        if (isExpiredTopic(normalizedTask)) {
            throw new RuntimeException("话题帖已截止，暂不支持继续操作");
        }
    }

    private boolean isTaskLikedByUser(Task task, Long currentUserId) {
        Task normalizedTask = TaskModeResolver.normalize(task);
        if (normalizedTask == null || currentUserId == null || !TaskModeResolver.isTopicTask(normalizedTask)) {
            return false;
        }
        return taskLikeMapper.selectByTaskIdAndUserId(normalizedTask.getId(), currentUserId) != null;
    }

    private boolean isVisibleToUser(Task task, Long currentUserId) {
        if (task == null) {
            return false;
        }
        if (!"approved".equalsIgnoreCase(task.getReviewStatus())) {
            return currentUserId != null && currentUserId.equals(task.getRequesterId());
        }
        if (!isExpiredTopic(task)) {
            return true;
        }
        return currentUserId != null && currentUserId.equals(task.getRequesterId());
    }

    private boolean isVisibleInCommunityFeed(Task task) {
        return task != null && "approved".equalsIgnoreCase(task.getReviewStatus());
    }

    private boolean isExpiredTopic(Task task) {
        return "topic".equals(task.getTaskMode())
            && task.getExpiresAt() != null
            && task.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private LocalDateTime resolveExpiresAt(TaskCreateRequest request) {
        if (request.getExpiresAt() == null || request.getExpiresAt().isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(request.getExpiresAt().trim());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("截止时间格式不正确");
        }
    }

    private String resolveCategory(TaskCreateRequest request) {
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            return request.getCategory().trim();
        }

        if (request.getBadgeSecondary() == null) {
            return "跑腿代办";
        }

        switch (request.getBadgeSecondary()) {
            case "校园配送":
                return "跑腿代办";
            case "学业辅导":
                return "学习辅导";
            case "闲置交换":
                return "二手闲置";
            case "信息求助":
                return "打听求助";
            case "社交互助":
                return "恋爱交友";
            case "兼职机会":
                return "兼职招聘";
            default:
                return request.getBadgeSecondary();
        }
    }

    private String resolveTaskMode(TaskCreateRequest request) {
        String category = resolveCategory(request);

        if (TOPIC_CATEGORIES.contains(category)) {
            return "topic";
        }
        if (TASK_CATEGORIES.contains(category)) {
            return "task";
        }

        String impactText = request.getImpactText();
        if (impactText != null) {
            String normalized = impactText.trim();
            if ("secondhand".equals(normalized) || "help".equals(normalized) || "social".equals(normalized) || "job".equals(normalized)) {
                return "topic";
            }
            if ("errand".equals(normalized) || "study".equals(normalized)) {
                return "task";
            }
        }

        if (request.getTaskMode() != null && !request.getTaskMode().isBlank()) {
            return request.getTaskMode().trim();
        }

        return "task";
    }

    private void notifyTaskEvent(Long senderId, Long receiverId, Long taskId, String content) {
        if (senderId == null || receiverId == null || senderId.equals(receiverId)) {
            return;
        }
        messageService.sendSystemTaskMessage(senderId, receiverId, taskId, content);
    }

}
