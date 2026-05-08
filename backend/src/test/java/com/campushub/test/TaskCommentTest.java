package com.campushub.test;

import com.campushub.dto.TaskCommentCreateRequest;
import com.campushub.dto.TaskCreateRequest;
import com.campushub.entity.Task;
import com.campushub.entity.TaskComment;
import com.campushub.entity.User;
import com.campushub.service.MessageService;
import com.campushub.service.TaskCommentService;
import com.campushub.service.TaskService;
import com.campushub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskCommentTest extends IntegrationTestSupport {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskCommentService taskCommentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Test
    public void testCreateTopicCommentAddsPoints() {
        Task topicTask = taskService.createTask(buildTopicRequest(), 1L);
        User beforeCommentUser = userService.getUserById(2L);

        TaskCommentCreateRequest request = new TaskCommentCreateRequest();
        request.setContent("这个帖子我很感兴趣，想进一步了解。");

        TaskComment comment = taskCommentService.createComment(topicTask.getId(), request, 2L);
        User afterCommentUser = userService.getUserById(2L);

        assertNotNull(comment.getId());
        assertEquals(topicTask.getId(), comment.getTaskId());
        assertEquals(beforeCommentUser.getPoints() + 5, afterCommentUser.getPoints());
        assertTrue(messageService.getMessagesByReceiverId(1L).stream()
            .anyMatch(message -> message.getTaskId().equals(topicTask.getId()) && message.getContent().contains("帖子回复")));
    }

    @Test
    public void testReplyComment() {
        Task topicTask = taskService.createTask(buildTopicRequest(), 1L);

        TaskCommentCreateRequest firstRequest = new TaskCommentCreateRequest();
        firstRequest.setContent("原始评论");
        TaskComment parentComment = taskCommentService.createComment(topicTask.getId(), firstRequest, 2L);

        TaskCommentCreateRequest replyRequest = new TaskCommentCreateRequest();
        replyRequest.setParentId(parentComment.getId());
        replyRequest.setContent("这是一个回复");
        TaskComment replyComment = taskCommentService.createComment(topicTask.getId(), replyRequest, 3L);

        List<TaskComment> comments = taskCommentService.getCommentsByTaskId(topicTask.getId());
        assertEquals(parentComment.getId(), replyComment.getParentId());
        assertEquals(2, comments.size());
        assertTrue(messageService.getMessagesByReceiverId(2L).stream()
            .anyMatch(message -> message.getTaskId().equals(topicTask.getId()) && message.getContent().contains("评论回复")));
    }

    @Test
    public void testTaskModeCannotComment() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("跑腿任务");
        request.setDescription("去驿站帮忙取件");
        request.setCategory("跑腿代办");
        request.setTaskMode("task");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("宿舍楼下");
        request.setTimeText("今晚 9 点前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("10 元");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");
        Task task = taskService.createTask(request, 1L);

        TaskCommentCreateRequest commentRequest = new TaskCommentCreateRequest();
        commentRequest.setContent("我来接单");

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> taskCommentService.createComment(task.getId(), commentRequest, 2L)
        );
        assertEquals("当前内容不是话题帖", exception.getMessage());
    }

    @Test
    public void testLegacySecondhandTopicWithConflictingModeCanComment() {
        TaskCreateRequest request = buildTopicRequest();
        request.setCategory("二手闲置");
        request.setBadgeSecondary("闲置交换");
        request.setImpactText("secondhand");

        Task topicTask = taskService.createTask(request, 1L);
        topicTask.setTaskMode("task");

        TaskCommentCreateRequest commentRequest = new TaskCommentCreateRequest();
        commentRequest.setContent("二手话题帖评论兼容验证");

        TaskComment comment = taskCommentService.createComment(topicTask.getId(), commentRequest, 2L);
        assertNotNull(comment.getId());
        assertEquals(topicTask.getId(), comment.getTaskId());
    }

    @Test
    public void testLegacySocialTopicWithConflictingModeCanComment() {
        TaskCreateRequest request = buildTopicRequest();
        request.setTitle("交友贴");
        request.setDescription("测试恋爱交友旧数据评论兼容");
        request.setCategory("恋爱交友");
        request.setBadgeSecondary("社交互助");
        request.setImpactText("social");

        Task topicTask = taskService.createTask(request, 1L);
        topicTask.setTaskMode("task");

        TaskCommentCreateRequest commentRequest = new TaskCommentCreateRequest();
        commentRequest.setContent("恋爱交友话题帖评论兼容验证");

        TaskComment comment = taskCommentService.createComment(topicTask.getId(), commentRequest, 2L);
        assertNotNull(comment.getId());
        assertEquals(topicTask.getId(), comment.getTaskId());
    }

    @Test
    public void testLegacyJobTopicWithConflictingModeCanComment() {
        TaskCreateRequest request = buildTopicRequest();
        request.setTitle("兼职贴");
        request.setDescription("测试兼职招聘旧数据评论兼容");
        request.setCategory("兼职招聘");
        request.setBadgeSecondary("兼职机会");
        request.setImpactText("job");

        Task topicTask = taskService.createTask(request, 1L);
        topicTask.setTaskMode("task");

        TaskCommentCreateRequest commentRequest = new TaskCommentCreateRequest();
        commentRequest.setContent("兼职招聘话题帖评论兼容验证");

        TaskComment comment = taskCommentService.createComment(topicTask.getId(), commentRequest, 2L);
        assertNotNull(comment.getId());
        assertEquals(topicTask.getId(), comment.getTaskId());
    }

    @Test
    public void testLongTermTopicCanStillComment() {
        TaskCreateRequest request = buildTopicRequest();
        request.setTitle("长期有效帖子");
        request.setTimeText("长期有效");
        request.setExpiresAt(null);

        Task topicTask = taskService.createTask(request, 1L);

        TaskCommentCreateRequest commentRequest = new TaskCommentCreateRequest();
        commentRequest.setContent("长期有效帖子评论验证");

        TaskComment comment = taskCommentService.createComment(topicTask.getId(), commentRequest, 2L);
        assertNotNull(comment.getId());
        assertEquals(topicTask.getId(), comment.getTaskId());
    }

    @Test
    public void testCommentRewardDailyLimitIsTwentyPoints() {
        Task topicTask = taskService.createTask(buildTopicRequest(), 1L);
        User beforeCommentUser = userService.getUserById(2L);

        for (int index = 0; index < 5; index++) {
            TaskCommentCreateRequest request = new TaskCommentCreateRequest();
            request.setContent("评论奖励上限测试 " + index);
            taskCommentService.createComment(topicTask.getId(), request, 2L);
        }

        User afterCommentUser = userService.getUserById(2L);
        assertEquals(beforeCommentUser.getPoints() + 20, afterCommentUser.getPoints());
    }

    private TaskCreateRequest buildTopicRequest() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("转让九成新台灯");
        request.setDescription("毕业清仓，宿舍自提。");
        request.setCategory("二手闲置");
        request.setTaskMode("topic");
        request.setBadgePrimary("热帖");
        request.setBadgeSecondary("闲置交换");
        request.setLocationText("东区宿舍");
        request.setTimeText("本周内有效");
        request.setRewardTitle("互动奖励");
        request.setRewardText("评论可得 5 积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("secondhand");
        return request;
    }
}
