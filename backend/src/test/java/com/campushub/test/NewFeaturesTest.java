package com.campushub.test;

import com.campushub.dto.TaskCreateRequest;
import com.campushub.entity.Notification;
import com.campushub.entity.Task;
import com.campushub.entity.TaskFavorite;
import com.campushub.mapper.TaskMapper;
import com.campushub.service.NotificationService;
import com.campushub.service.TaskService;
import com.campushub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NewFeaturesTest extends IntegrationTestSupport {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // ===== Notification Tests =====

    @Test
    public void testCreateNotification() {
        notificationService.createNotification(1L, "TEST", "测试标题", "测试内容", "task", null);
        List<Notification> notifications = notificationService.getNotifications(1L);
        assertFalse(notifications.isEmpty());
        Notification n = notifications.get(0);
        assertEquals("测试标题", n.getTitle());
        assertEquals("TEST", n.getType());
        assertFalse(n.getIsRead());
    }

    @Test
    public void testMarkNotificationAsRead() {
        notificationService.createNotification(1L, "SYSTEM", "通知一", "内容一", null, null);
        List<Notification> notifications = notificationService.getNotifications(1L);
        assertFalse(notifications.isEmpty());

        notificationService.markAsRead(notifications.get(0).getId(), 1L);
        List<Notification> after = notificationService.getNotifications(1L);
        assertTrue(after.get(0).getIsRead());
    }

    @Test
    public void testMarkAllNotificationsRead() {
        notificationService.createNotification(1L, "SYS", "A", "a", null, null);
        notificationService.createNotification(1L, "SYS", "B", "b", null, null);
        notificationService.markAllRead(1L);
        List<Notification> list = notificationService.getNotifications(1L);
        for (Notification n : list) {
            assertTrue(n.getIsRead());
        }
    }

    @Test
    public void testUnreadCount() {
        int before = notificationService.getUnreadCount(1L);
        notificationService.createNotification(1L, "SYS", "新通知", "content", null, null);
        int after = notificationService.getUnreadCount(1L);
        assertEquals(before + 1, after);
    }

    @Test
    public void testNotificationOwnershipCheck() {
        notificationService.createNotification(1L, "SYS", "用户1的通知", "x", null, null);
        List<Notification> list = notificationService.getNotifications(1L);
        assertFalse(list.isEmpty());
        Long notifId = list.get(0).getId();

        assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(notifId, 2L);
        });
    }

    // ===== Search Tests =====

    @Test
    public void testSearchTasks() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("校园跑腿代取快递");
        request.setDescription("帮忙从校门口取快递送到宿舍楼下");
        request.setBadgePrimary("紧急");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("南门快递站");
        request.setTimeText("今天下午");
        request.setRewardTitle("奖励");
        request.setRewardText("15元");
        request.setImpactTitle("帮助同学");
        request.setImpactText("errand");
        taskService.createTask(request, 1L);

        List<Task> results = taskMapper.searchTasks("代取快递", null, 0, 20);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testSearchTasksNoResults() {
        List<Task> results = taskMapper.searchTasks("不存在的搜索词xyzabc", null, 0, 20);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchTasksByMode() {
        TaskCreateRequest taskReq = new TaskCreateRequest();
        taskReq.setTitle("帮忙代课");
        taskReq.setDescription("周三下午需要代课");
        taskReq.setBadgePrimary("普通");
        taskReq.setBadgeSecondary("学业辅导");
        taskReq.setLocationText("教学楼A");
        taskReq.setTimeText("周三下午2点");
        taskReq.setRewardTitle("报酬");
        taskReq.setRewardText("20元");
        taskReq.setImpactTitle("帮助");
        taskReq.setImpactText("study");
        taskService.createTask(taskReq, 1L);

        List<Task> taskResults = taskMapper.searchTasks("代课", "task", 0, 20);
        assertFalse(taskResults.isEmpty());
        List<Task> topicResults = taskMapper.searchTasks("代课", "topic", 0, 20);
        assertTrue(topicResults.isEmpty());
    }

    // ===== Favorites Tests =====

    @Test
    public void testFavoriteAndUnfavoriteTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("收藏测试任务");
        request.setDescription("测试收藏功能");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("校内");
        request.setTimeText("明天");
        request.setRewardTitle("奖励");
        request.setRewardText("10元");
        request.setImpactTitle("帮助");
        request.setImpactText("errand");
        Task task = taskService.createTask(request, 1L);

        // Record baseline count before favoriting
        int beforeCount = taskService.getFavoriteTasks(2L).size();

        // User 2 favorites the task
        taskService.favoriteTask(task.getId(), 2L);
        List<Task> favorites = taskService.getFavoriteTasks(2L);
        assertEquals(beforeCount + 1, favorites.size());
        assertTrue(favorites.stream().anyMatch(t -> t.getId().equals(task.getId())));

        // User 2 unfavorites the task
        taskService.unfavoriteTask(task.getId(), 2L);

        // Verify the count went back to baseline (the task is no longer favorited)
        List<Task> afterUnfavorite = taskService.getFavoriteTasks(2L);
        assertEquals(beforeCount, afterUnfavorite.size(),
            "Expected " + beforeCount + " after unfavorite, got " + afterUnfavorite.size() +
            ": " + afterUnfavorite.stream().map(t -> t.getId() + "=" + t.getTitle()).toList());
        assertTrue(afterUnfavorite.stream().noneMatch(t -> t.getId().equals(task.getId())));
    }

    @Test
    public void testCannotFavoriteTwice() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("重复收藏测试");
        request.setDescription("测试");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("校内");
        request.setTimeText("明天");
        request.setRewardTitle("奖励");
        request.setRewardText("5元");
        request.setImpactTitle("帮助");
        request.setImpactText("errand");
        Task task = taskService.createTask(request, 1L);

        taskService.favoriteTask(task.getId(), 2L);
        assertThrows(RuntimeException.class, () -> {
            taskService.favoriteTask(task.getId(), 2L);
        });
    }

    // ===== Image Upload Test =====

    @Test
    public void testCreateTaskWithImageUrls() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("带图片的任务");
        request.setDescription("包含图片的任务测试");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("校内");
        request.setTimeText("明天");
        request.setRewardTitle("奖励");
        request.setRewardText("10元");
        request.setImpactTitle("帮助");
        request.setImpactText("errand");
        request.setImageUrls("[\"/files/test.jpg\"]");

        Task task = taskService.createTask(request, 1L);
        assertNotNull(task.getImageUrls());
        assertTrue(task.getImageUrls().contains("test.jpg"));
    }

    // ===== Leaderboard Test =====

    @Test
    public void testGetLeaderboard() {
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(5);
        assertNotNull(leaderboard);
        assertTrue(leaderboard.size() <= 5);
    }

    // ===== Entity Lifecycle Tests =====

    @Test
    public void testNotificationEntity() {
        Notification n = new Notification();
        LocalDateTime now = LocalDateTime.now();
        n.setId(1L);
        n.setUserId(2L);
        n.setType("TASK_ACCEPTED");
        n.setTitle("有人接了你的任务");
        n.setContent("详细内容");
        n.setReferenceType("task");
        n.setReferenceId(10L);
        n.setIsRead(false);
        n.setCreatedAt(now);

        assertEquals(1L, n.getId());
        assertEquals(2L, n.getUserId());
        assertEquals("TASK_ACCEPTED", n.getType());
        assertEquals("有人接了你的任务", n.getTitle());
        assertEquals("详细内容", n.getContent());
        assertEquals("task", n.getReferenceType());
        assertEquals(10L, n.getReferenceId());
        assertFalse(n.getIsRead());
        assertEquals(now, n.getCreatedAt());
    }

    @Test
    public void testTaskFavoriteEntity() {
        TaskFavorite f = new TaskFavorite();
        LocalDateTime now = LocalDateTime.now();
        f.setId(1L);
        f.setUserId(2L);
        f.setTaskId(3L);
        f.setCreatedAt(now);

        assertEquals(1L, f.getId());
        assertEquals(2L, f.getUserId());
        assertEquals(3L, f.getTaskId());
        assertEquals(now, f.getCreatedAt());
    }

    // ===== Scheduled Task Test =====

    @Test
    public void testCancelExpiredTasks() {
        taskService.cancelExpiredTasks();
        // Should not throw; verifies the scheduled method runs without error
    }
}
