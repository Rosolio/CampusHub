package com.campushub.service;

import com.campushub.dto.TaskCommentCreateRequest;
import com.campushub.entity.Task;
import com.campushub.entity.TaskComment;
import com.campushub.entity.TaskCommentLike;
import com.campushub.mapper.TaskCommentMapper;
import com.campushub.mapper.TaskCommentLikeMapper;
import com.campushub.mapper.TaskMapper;
import com.campushub.util.TaskModeResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskCommentService {
    private static final int COMMENT_REWARD_POINTS = 5;
    private static final int COMMENT_REWARD_DAILY_LIMIT = 20;

    private final TaskCommentMapper taskCommentMapper;
    private final TaskCommentLikeMapper taskCommentLikeMapper;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final MessageService messageService;

    public TaskCommentService(
        TaskCommentMapper taskCommentMapper,
        TaskCommentLikeMapper taskCommentLikeMapper,
        TaskMapper taskMapper,
        UserService userService,
        MessageService messageService
    ) {
        this.taskCommentMapper = taskCommentMapper;
        this.taskCommentLikeMapper = taskCommentLikeMapper;
        this.taskMapper = taskMapper;
        this.userService = userService;
        this.messageService = messageService;
    }

    public List<TaskComment> getCommentsByTaskId(Long taskId) {
        return getCommentsByTaskId(taskId, null);
    }

    public List<TaskComment> getCommentsByTaskId(Long taskId, Long currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateTopicTask(task);
        List<TaskComment> comments = taskCommentMapper.selectByTaskId(taskId);

        if (currentUserId != null && !comments.isEmpty()) {
            List<Long> commentIds = comments.stream().map(TaskComment::getId).toList();
            List<Long> likedCommentIds = taskCommentLikeMapper.selectLikedCommentIds(commentIds, currentUserId);
            java.util.Set<Long> likedSet = new java.util.HashSet<>(likedCommentIds);
            comments.forEach(comment -> comment.setLikedByCurrentUser(likedSet.contains(comment.getId())));
        }

        return comments;
    }

    @Transactional
    public TaskComment createComment(Long taskId, TaskCommentCreateRequest request, Long authorId) {
        Task task = taskMapper.selectById(taskId);
        validateInteractiveTopicTask(task);

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }

        if (request.getParentId() != null) {
            TaskComment parentComment = taskCommentMapper.selectById(request.getParentId());
            if (parentComment == null || !taskId.equals(parentComment.getTaskId())) {
                throw new RuntimeException("回复的评论不存在");
            }
        }

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setAuthorId(authorId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent().trim());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        taskCommentMapper.insert(comment);

        int availableCommentRewardPoints = Math.max(0, COMMENT_REWARD_DAILY_LIMIT - userService.getTodayCommentRewardPoints(authorId));
        int awardedCommentPoints = Math.min(COMMENT_REWARD_POINTS, availableCommentRewardPoints);
        if (awardedCommentPoints > 0) {
            userService.addPoints(
                authorId,
                awardedCommentPoints,
                "COMMENT_REWARD",
                request.getParentId() == null ? "发布评论奖励" : "发布回复奖励",
                "task_comment",
                comment.getId()
            );
        }
        TaskComment savedComment = taskCommentMapper.selectById(comment.getId());
        notifyCommentEvent(task, savedComment);
        return savedComment;
    }

    @Transactional
    public TaskComment likeComment(Long taskId, Long commentId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        validateInteractiveTopicTask(task);

        TaskComment comment = taskCommentMapper.selectById(commentId);
        if (comment == null || !taskId.equals(comment.getTaskId())) {
            throw new RuntimeException("评论不存在");
        }
        if (userId.equals(comment.getAuthorId())) {
            throw new RuntimeException("不能给自己的评论点赞");
        }
        if (taskCommentLikeMapper.selectByCommentIdAndUserId(commentId, userId) != null) {
            throw new RuntimeException("你已经点过赞了");
        }

        TaskCommentLike like = new TaskCommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreatedAt(LocalDateTime.now());
        taskCommentLikeMapper.insert(like);

        comment.setLikeCount((comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1);
        comment.setUpdatedAt(LocalDateTime.now());
        taskCommentMapper.update(comment);
        userService.addPoints(comment.getAuthorId(), 1, "COMMENT_LIKE_REWARD", "评论获赞奖励", "task_comment", commentId);

        TaskComment refreshedComment = taskCommentMapper.selectById(commentId);
        refreshedComment.setLikedByCurrentUser(true);
        return refreshedComment;
    }

    @Transactional
    public TaskComment unlikeComment(Long taskId, Long commentId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        validateInteractiveTopicTask(task);

        TaskComment comment = taskCommentMapper.selectById(commentId);
        if (comment == null || !taskId.equals(comment.getTaskId())) {
            throw new RuntimeException("评论不存在");
        }
        if (taskCommentLikeMapper.selectByCommentIdAndUserId(commentId, userId) == null) {
            throw new RuntimeException("你还没有点过赞");
        }

        taskCommentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);

        comment.setLikeCount(Math.max(0, (comment.getLikeCount() == null ? 0 : comment.getLikeCount()) - 1));
        comment.setUpdatedAt(LocalDateTime.now());
        taskCommentMapper.update(comment);
        userService.addPoints(comment.getAuthorId(), -1, "COMMENT_LIKE_REWARD_REVOKE", "评论取消点赞扣回", "task_comment", commentId);

        TaskComment refreshedComment = taskCommentMapper.selectById(commentId);
        refreshedComment.setLikedByCurrentUser(false);
        return refreshedComment;
    }

    @Transactional
    public void deleteComment(Long taskId, Long commentId, Long currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateInteractiveTopicTask(task);

        TaskComment comment = taskCommentMapper.selectById(commentId);
        if (comment == null || !taskId.equals(comment.getTaskId())) {
            throw new RuntimeException("评论不存在");
        }

        boolean canDelete = currentUserId.equals(task.getRequesterId()) || currentUserId.equals(comment.getAuthorId());
        if (!canDelete) {
            throw new RuntimeException("只有帖主或评论发布者可以删除这条评论");
        }

        List<Long> commentIds = new ArrayList<>();
        collectCommentIds(commentId, commentIds);
        if (commentIds.isEmpty()) {
            return;
        }

        taskCommentLikeMapper.deleteByCommentIds(commentIds);
        taskCommentMapper.deleteByIds(commentIds);
    }

    public void deleteByTaskId(Long taskId) {
        taskCommentMapper.deleteByTaskId(taskId);
    }

    private void collectCommentIds(Long commentId, List<Long> collector) {
        collector.add(commentId);
        List<TaskComment> children = taskCommentMapper.selectByParentId(commentId);
        for (TaskComment child : children) {
            collectCommentIds(child.getId(), collector);
        }
    }

    private void validateTopicTask(Task task) {
        Task normalizedTask = TaskModeResolver.normalize(task);
        if (normalizedTask == null) {
            throw new RuntimeException("帖子不存在");
        }
        if (!TaskModeResolver.isTopicTask(normalizedTask)) {
            throw new RuntimeException("当前内容不是话题帖");
        }
    }

    private void validateInteractiveTopicTask(Task task) {
        validateTopicTask(task);
        if (task.getExpiresAt() != null && task.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("话题帖已截止，暂不支持继续操作");
        }
    }

    private void notifyCommentEvent(Task task, TaskComment comment) {
        if (task == null || comment == null || comment.getAuthorId() == null) {
            return;
        }

        String authorName = comment.getAuthorName() != null && !comment.getAuthorName().isBlank()
            ? comment.getAuthorName()
            : "有同学";

        if (comment.getParentId() != null) {
            TaskComment parentComment = taskCommentMapper.selectById(comment.getParentId());
            if (parentComment != null && parentComment.getAuthorId() != null && !comment.getAuthorId().equals(parentComment.getAuthorId())) {
                messageService.sendSystemTaskMessage(
                    comment.getAuthorId(),
                    parentComment.getAuthorId(),
                    task.getId(),
                    String.format("【评论回复】%s 回复了你在《%s》下的评论，点击查看帖子详情。", authorName, task.getTitle())
                );
            }
            return;
        }

        if (task.getRequesterId() != null && !comment.getAuthorId().equals(task.getRequesterId())) {
            messageService.sendSystemTaskMessage(
                comment.getAuthorId(),
                task.getRequesterId(),
                task.getId(),
                String.format("【帖子回复】%s 评论了你发布的话题帖《%s》，点击查看帖子详情。", authorName, task.getTitle())
            );
        }
    }
}
