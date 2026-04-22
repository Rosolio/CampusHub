package com.campusaid.test;

import com.campusaid.dto.TaskCreateRequest;
import com.campusaid.entity.Task;
import com.campusaid.mapper.TaskMapper;
import com.campusaid.mapper.UserMapper;
import com.campusaid.service.MessageService;
import com.campusaid.service.TaskService;
import com.campusaid.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TaskTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Test
    public void testCreateTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("测试任务");
        request.setDescription("这是一个测试任务");
        request.setBadgePrimary("紧急");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("南楼枢纽快递点");
        request.setTimeText("今天, 下午 5:00 前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("赚取 15 校园积分");
        request.setImpactTitle("社区影响");
        request.setImpactText("帮助同学按时取件");

        Task task = taskService.createTask(request, 1L);
        assertNotNull(task);
        assertEquals("测试任务", task.getTitle());
    }

    @Test
    public void testCreateTopicTaskWithContactInfo() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("带联系方式的话题帖");
        request.setDescription("可选联系方式应被保存");
        request.setCategory("打听求助");
        request.setTaskMode("topic");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary("信息求助");
        request.setLocationText("线上交流");
        request.setTimeText("本周内");
        request.setRewardTitle("互动奖励");
        request.setRewardText("评论可得 5 积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("help");
        request.setContactInfo("微信 campus-aid");

        Task task = taskService.createTask(request, 1L);
        assertNotNull(task);
        assertEquals("微信 campus-aid", taskService.getTaskById(task.getId()).getContactInfo());
    }

    @Test
    public void testCreateLongTermTopicTaskWithoutExpiresAt() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("长期有效的话题帖");
        request.setDescription("不设置截止时间也应允许发布");
        request.setCategory("打听求助");
        request.setTaskMode("topic");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary("信息求助");
        request.setLocationText("线上交流");
        request.setTimeText("长期有效");
        request.setRewardTitle("互动奖励");
        request.setRewardText("评论可得 5 积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("help");

        Task task = taskService.createTask(request, 1L);

        assertNotNull(task);
        assertNull(task.getExpiresAt());
        assertEquals("长期有效", taskService.getTaskById(task.getId()).getTimeText());
    }

    @Test
    public void testGetTasks() {
        List<Task> tasks = taskService.getTasks();
        assertNotNull(tasks);
    }

    @Test
    public void testGetTaskById() {
        // 先创建一个任务
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("测试任务");
        request.setDescription("这是一个测试任务");
        request.setBadgePrimary("紧急");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("南楼枢纽快递点");
        request.setTimeText("今天, 下午 5:00 前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("赚取 15 校园积分");
        request.setImpactTitle("社区影响");
        request.setImpactText("帮助同学按时取件");

        Task createdTask = taskService.createTask(request, 1L);
        Long taskId = createdTask.getId();

        // 测试获取任务详情
        Task task = taskService.getTaskById(taskId);
        assertNotNull(task);
        assertEquals(taskId, task.getId());
    }

    @Test
    public void testAcceptTask() {
        // 先创建一个任务
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("测试任务");
        request.setDescription("这是一个测试任务");
        request.setBadgePrimary("紧急");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("南楼枢纽快递点");
        request.setTimeText("今天, 下午 5:00 前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("赚取 15 校园积分");
        request.setImpactTitle("社区影响");
        request.setImpactText("帮助同学按时取件");

        Task createdTask = taskService.createTask(request, 1L);
        Long taskId = createdTask.getId();

        // 测试接受任务
        Task acceptedTask = taskService.acceptTask(taskId, 2L);
        assertNotNull(acceptedTask);
        assertEquals("accepted", acceptedTask.getStatus());
        assertTrue(messageService.getMessagesByReceiverId(1L).stream()
            .anyMatch(message -> message.getTaskId().equals(taskId) && message.getContent().contains("接单通知")));
    }

    @Test
    public void testCompleteTaskAwardsPointsToRequesterAndHelper() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("完成奖励测试");
        request.setDescription("测试任务完成后的积分发放");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("图书馆");
        request.setTimeText("今晚前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("完成后加积分");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");

        Task createdTask = taskService.createTask(request, 1L);
        taskService.acceptTask(createdTask.getId(), 2L);

        int requesterPointsBefore = userMapper.selectById(1L).getPoints();
        int helperPointsBefore = userMapper.selectById(2L).getPoints();

        Task pendingCompletionTask = taskService.completeTask(createdTask.getId(), 1L);
        assertEquals("completion_pending", pendingCompletionTask.getStatus());
        assertEquals(requesterPointsBefore, userMapper.selectById(1L).getPoints());
        assertEquals(helperPointsBefore, userMapper.selectById(2L).getPoints());
        assertTrue(messageService.getMessagesByReceiverId(2L).stream()
            .anyMatch(message -> message.getTaskId().equals(createdTask.getId()) && message.getContent().contains("完成确认")));

        Task completedTask = taskService.completeTask(createdTask.getId(), 2L);
        assertEquals("completed", completedTask.getStatus());
        assertEquals(requesterPointsBefore + 10, userMapper.selectById(1L).getPoints());
        assertEquals(helperPointsBefore + 10, userMapper.selectById(2L).getPoints());
        assertTrue(messageService.getMessagesByReceiverId(1L).stream()
            .anyMatch(message -> message.getTaskId().equals(createdTask.getId()) && message.getContent().contains("等待互评")));
    }

    @Test
    public void testOnlyTaskParticipantsCanCompleteTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("权限测试");
        request.setDescription("只有发布者可完成");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("食堂");
        request.setTimeText("明天前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("完成后加积分");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");

        Task createdTask = taskService.createTask(request, 1L);
        taskService.acceptTask(createdTask.getId(), 2L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.completeTask(createdTask.getId(), 3L));
        assertEquals("只有任务双方可以确认完成", exception.getMessage());
    }

    @Test
    public void testDeleteTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("待删除任务");
        request.setDescription("这是一个待删除的测试任务");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("图书馆");
        request.setTimeText("今天, 下午 6:00 前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("10 积分");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");

        Task createdTask = taskService.createTask(request, 1L);
        Long taskId = createdTask.getId();

        taskService.deleteTask(taskId, 1L);

        Task deletedTask = taskService.getTaskById(taskId);
        assertNull(deletedTask);
    }

    @Test
    public void testLikeLegacyTopicTaskWithConflictingMode() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("旧话题帖");
        request.setDescription("测试旧数据点赞兼容");
        request.setCategory("打听求助");
        request.setTaskMode("topic");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary("信息求助");
        request.setLocationText("教学楼");
        request.setTimeText("本周有效");
        request.setRewardTitle("互动奖励");
        request.setRewardText("点赞加积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("help");

        Task createdTask = taskService.createTask(request, 1L);
        createdTask.setTaskMode("task");
        taskMapper.update(createdTask);

        Task likedTask = taskService.likeTask(createdTask.getId(), 2L);
        assertNotNull(likedTask);
        assertEquals("topic", likedTask.getTaskMode());
        assertEquals(1, likedTask.getLikeCount());
        assertTrue(Boolean.TRUE.equals(likedTask.getLikedByCurrentUser()));
    }

    @Test
    public void testLikeLegacySecondhandTopicTaskWithConflictingMode() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("二手话题帖");
        request.setDescription("测试二手闲置旧数据点赞兼容");
        request.setCategory("二手闲置");
        request.setTaskMode("topic");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary("闲置交换");
        request.setLocationText("宿舍");
        request.setTimeText("本周有效");
        request.setRewardTitle("互动奖励");
        request.setRewardText("点赞加积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("secondhand");

        Task createdTask = taskService.createTask(request, 1L);
        createdTask.setTaskMode("task");
        taskMapper.update(createdTask);

        Task likedTask = taskService.likeTask(createdTask.getId(), 2L);
        assertEquals("topic", likedTask.getTaskMode());
        assertEquals(1, likedTask.getLikeCount());
        assertTrue(Boolean.TRUE.equals(likedTask.getLikedByCurrentUser()));
    }

    @Test
    public void testLikeLegacySocialTopicTaskWithConflictingMode() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("交友话题帖");
        request.setDescription("测试恋爱交友旧数据点赞兼容");
        request.setCategory("恋爱交友");
        request.setTaskMode("topic");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary("社交互助");
        request.setLocationText("校园");
        request.setTimeText("本周有效");
        request.setRewardTitle("互动奖励");
        request.setRewardText("点赞加积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("social");

        Task createdTask = taskService.createTask(request, 1L);
        createdTask.setTaskMode("task");
        taskMapper.update(createdTask);

        Task likedTask = taskService.likeTask(createdTask.getId(), 2L);
        assertEquals("topic", likedTask.getTaskMode());
        assertEquals(1, likedTask.getLikeCount());
    }

    @Test
    public void testLikeLegacyJobTopicTaskWithConflictingMode() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("兼职话题帖");
        request.setDescription("测试兼职招聘旧数据点赞兼容");
        request.setCategory("兼职招聘");
        request.setTaskMode("topic");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary("兼职机会");
        request.setLocationText("校园");
        request.setTimeText("本周有效");
        request.setRewardTitle("互动奖励");
        request.setRewardText("点赞加积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText("job");

        Task createdTask = taskService.createTask(request, 1L);
        createdTask.setTaskMode("task");
        taskMapper.update(createdTask);

        Task likedTask = taskService.likeTask(createdTask.getId(), 2L);
        assertEquals("topic", likedTask.getTaskMode());
        assertEquals(1, likedTask.getLikeCount());
    }

}
