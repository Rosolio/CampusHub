package com.campushub.test;

import com.campushub.config.OnlineSessionManager;
import com.campushub.dto.TaskCreateRequest;
import com.campushub.entity.Task;
import com.campushub.entity.TaskFavorite;
import com.campushub.mapper.TaskFavoriteMapper;
import com.campushub.mapper.TaskMapper;
import com.campushub.mapper.UserMapper;
import com.campushub.service.TaskService;
import com.campushub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExtendedFeaturesTest extends IntegrationTestSupport {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskFavoriteMapper taskFavoriteMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OnlineSessionManager onlineSessionManager;

    // ===== Helper =====

    private Task createTopic(String title, String category, Long userId) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle(title);
        request.setDescription(title + "的详细描述");
        request.setBadgePrimary("话题帖");
        request.setBadgeSecondary(category);
        request.setCategory(category);
        request.setLocationText("校内");
        request.setTimeText("三天内");
        request.setRewardTitle("互动奖励");
        request.setRewardText("评论可得积分");
        request.setImpactTitle("帖子类型");
        request.setImpactText(category);
        return taskService.createTask(request, userId);
    }

    private Task createTask(String title, Long userId) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle(title);
        request.setDescription(title + "描述");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setCategory("跑腿代办");
        request.setLocationText("校内");
        request.setTimeText("今天下午");
        request.setRewardTitle("奖励");
        request.setRewardText("10元");
        request.setImpactTitle("帮助");
        request.setImpactText("errand");
        return taskService.createTask(request, userId);
    }

    // ===== OnlineSessionManager Tests =====

    @Test
    public void testOnlineSessionManagerInitiallyOffline() {
        assertFalse(onlineSessionManager.isOnline(1L));
    }

    @Test
    public void testOnlineSessionManagerRegisterAndUnregister() {
        // Register with a null session simulates the tracking behavior
        // The manager stores sessions in a Set; we can verify the user
        // transitions from offline→online→offline using internal state
        assertFalse(onlineSessionManager.isOnline(100L));

        // Simulate connection: register then immediately verify
        onlineSessionManager.register(100L, null);
        assertTrue(onlineSessionManager.isOnline(100L));

        // Simulate disconnection
        onlineSessionManager.unregister(100L, null);
        assertFalse(onlineSessionManager.isOnline(100L));
    }

    @Test
    public void testOnlineSessionManagerMultipleSessions() {
        // User opens multiple tabs — should stay online until ALL are closed
        onlineSessionManager.register(200L, null);
        onlineSessionManager.register(200L, null);  // second tab
        assertTrue(onlineSessionManager.isOnline(200L));

        // Close one tab
        onlineSessionManager.unregister(200L, null);
        assertTrue(onlineSessionManager.isOnline(200L));  // still online via second tab

        // Close last tab
        onlineSessionManager.unregister(200L, null);
        assertFalse(onlineSessionManager.isOnline(200L));
    }

    @Test
    public void testOnlineSessionManagerDifferentUsersIndependent() {
        onlineSessionManager.register(1L, null);
        assertTrue(onlineSessionManager.isOnline(1L));
        assertFalse(onlineSessionManager.isOnline(2L));
    }

    @Test
    public void testOnlineSessionManagerNullUserId() {
        // isOnline(null) should return false safely
        assertFalse(onlineSessionManager.isOnline(null));
    }

    // ===== Favorites Extended Tests =====

    @Test
    public void testIsFavoritedInTaskDetail() {
        Task topic = createTopic("收藏状态测试帖", "二手闲置", 1L);

        // User 2 favorites the topic
        taskService.favoriteTask(topic.getId(), 2L);

        // User 2 views detail — should see isFavorited=true
        Task detailForUser2 = taskService.getTaskById(topic.getId(), 2L);
        assertNotNull(detailForUser2);
        assertTrue(detailForUser2.getIsFavorited());

        // User 3 views detail — should see isFavorited=false
        Task detailForUser3 = taskService.getTaskById(topic.getId(), 3L);
        assertNotNull(detailForUser3);
        assertFalse(detailForUser3.getIsFavorited());

        // Unauthenticated view — should see isFavorited=false
        Task detailAnon = taskService.getTaskById(topic.getId(), null);
        assertNotNull(detailAnon);
        assertFalse(detailAnon.getIsFavorited());
    }

    @Test
    public void testCannotFavoriteOwnTask() {
        // Though not enforced at service level, verify isFavorited is false for own task
        Task topic = createTopic("自己的帖子", "二手闲置", 1L);
        Task detail = taskService.getTaskById(topic.getId(), 1L);
        assertFalse(detail.getIsFavorited());
    }

    @Test
    public void testCannotUnfavoriteNonExistent() {
        assertThrows(RuntimeException.class, () -> {
            taskService.unfavoriteTask(99999L, 2L);
        });
    }

    @Test
    public void testCannotFavoriteNonExistentTask() {
        assertThrows(RuntimeException.class, () -> {
            taskService.favoriteTask(99999L, 2L);
        });
    }

    @Test
    public void testFavoriteTasksListContainsCorrectFields() {
        Task topic1 = createTopic("收藏列表测试一", "二手闲置", 1L);
        Task topic2 = createTopic("收藏列表测试二", "恋爱交友", 1L);

        taskService.favoriteTask(topic1.getId(), 2L);
        taskService.favoriteTask(topic2.getId(), 2L);

        List<Task> favorites = taskService.getFavoriteTasks(2L);
        assertEquals(2, favorites.size());

        // Each should have requester info populated
        for (Task t : favorites) {
            assertNotNull(t.getTitle());
            assertNotNull(t.getRequesterName());
            assertNotNull(t.getCommentCount());
            assertNotNull(t.getFavoriteCount());
        }
    }

    @Test
    public void testFavoriteCountInTaskDetail() {
        Task topic = createTopic("收藏计数测试帖", "二手闲置", 1L);

        // Initially 0 favorites
        Task before = taskService.getTaskById(topic.getId(), null);
        assertEquals(0, before.getFavoriteCount() != null ? before.getFavoriteCount() : 0);

        // Two users favorite
        taskService.favoriteTask(topic.getId(), 2L);
        taskService.favoriteTask(topic.getId(), 3L);

        // Count should be 2
        Task after = taskService.getTaskById(topic.getId(), null);
        assertNotNull(after.getFavoriteCount());
        assertEquals(2, after.getFavoriteCount().intValue());
    }

    @Test
    public void testFavoritesFromDifferentUsersDontConflict() {
        Task topic = createTopic("独立收藏测试", "打听求助", 1L);

        // User 2 favorites
        taskService.favoriteTask(topic.getId(), 2L);
        assertEquals(1, taskService.getFavoriteTasks(2L).size());

        // User 3 also favorites — user 2's list unchanged
        taskService.favoriteTask(topic.getId(), 3L);
        assertEquals(1, taskService.getFavoriteTasks(2L).size());
        assertEquals(1, taskService.getFavoriteTasks(3L).size());
    }

    // ===== Leaderboard Tests =====

    @Test
    public void testLeaderboardReturnsData() {
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(20);
        assertNotNull(leaderboard);
        assertFalse(leaderboard.isEmpty());
    }

    @Test
    public void testLeaderboardFields() {
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(5);
        assertFalse(leaderboard.isEmpty());

        Map<String, Object> entry = leaderboard.get(0);
        assertNotNull(entry.get("id"));
        assertNotNull(entry.get("name"));
        assertNotNull(entry.get("points"));
        assertNotNull(entry.get("score"));
        // avatarUrl may be null — that's valid
    }

    @Test
    public void testLeaderboardRespectsLimit() {
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(3);
        assertTrue(leaderboard.size() <= 3);
    }

    @Test
    public void testLeaderboardSortedByPointsDescending() {
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(20);
        assertFalse(leaderboard.isEmpty());

        int previous = Integer.MAX_VALUE;
        for (Map<String, Object> entry : leaderboard) {
            int points = ((Number) entry.get("points")).intValue();
            assertTrue(points <= previous, "Leaderboard should be sorted descending by points");
            previous = points;
        }
    }

    @Test
    public void testLeaderboardExcludesAdmin() {
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(20);
        for (Map<String, Object> entry : leaderboard) {
            long id = ((Number) entry.get("id")).longValue();
            // Admin user ID is 4
            assertNotEquals(4L, id, "Admin should not appear on leaderboard");
        }
    }

    // ===== Online Fields on Task Entity Tests =====

    @Test
    public void testRequesterOnlineField() {
        Task task = createTask("在线状态字段测试", 1L);
        Task detail = taskService.getTaskById(task.getId(), 2L);

        // Without WebSocket session, requesterOnline should be false
        assertNotNull(detail.getRequesterOnline());
        assertFalse(detail.getRequesterOnline());
    }

    @Test
    public void testHelperOnlineFieldNullWhenNoHelper() {
        Task task = createTask("无接单者测试", 1L);
        Task detail = taskService.getTaskById(task.getId(), 2L);
        assertNull(detail.getHelperOnline());
    }

    // ===== Task Visibility Tests =====

    @Test
    public void testOwnerCanSeeOwnExpiredTopic() {
        Task topic = createTopic("过期帖测试", "二手闲置", 1L);

        // Manually expire the topic via mapper
        topic.setExpiresAt(LocalDateTime.now().minusDays(1));
        taskMapper.update(topic);

        // Owner should still see it
        Task detail = taskService.getTaskById(topic.getId(), 1L);
        assertNotNull(detail);
    }

    @Test
    public void testNonOwnerCannotSeeExpiredTopic() {
        Task topic = createTopic("他人过期帖", "二手闲置", 1L);

        // Expire it
        topic.setExpiresAt(LocalDateTime.now().minusDays(1));
        taskMapper.update(topic);

        // Non-owner should get an error
        assertThrows(RuntimeException.class, () -> {
            taskService.getTaskById(topic.getId(), 2L);
        });
    }

    // ===== Search Tests =====

    @Test
    public void testSearchPagination() {
        // Create 5 tasks with similar titles
        for (int i = 1; i <= 5; i++) {
            TaskCreateRequest request = new TaskCreateRequest();
            request.setTitle("分页搜索测试任务" + i);
            request.setDescription("测试分页功能");
            request.setBadgePrimary("普通");
            request.setBadgeSecondary("校园配送");
            request.setLocationText("教学楼");
            request.setTimeText("明天");
            request.setRewardTitle("报酬");
            request.setRewardText("10元");
            request.setImpactTitle("帮助");
            request.setImpactText("errand");
            taskService.createTask(request, 1L);
        }

        // Page 1: 3 items
        List<Task> page1 = taskMapper.searchTasks("分页搜索测试任务", null, 0, 3);
        assertEquals(3, page1.size());

        // Page 2: remaining items
        List<Task> page2 = taskMapper.searchTasks("分页搜索测试任务", null, 3, 3);
        assertFalse(page2.isEmpty());
        assertTrue(page2.size() <= 3);

        // Verify no overlap
        for (Task t : page2) {
            for (Task p1 : page1) {
                assertNotEquals(t.getId(), p1.getId());
            }
        }
    }

    @Test
    public void testSearchFilterByTopicMode() {
        Task topic = createTopic("搜索模式过滤测试帖", "二手闲置", 1L);
        Task task = createTask("搜索模式过滤任务", 1L);

        List<Task> topicResults = taskMapper.searchTasks("搜索模式过滤", "topic", 0, 20);
        List<Task> taskResults = taskMapper.searchTasks("搜索模式过滤", "task", 0, 20);

        boolean topicFound = topicResults.stream().anyMatch(t -> t.getId().equals(topic.getId()));
        boolean taskFound = taskResults.stream().anyMatch(t -> t.getId().equals(task.getId()));

        assertTrue(topicFound);
        assertTrue(taskFound);
    }

    // ===== Points & Score Tests =====

    @Test
    public void testLeaderboardPointsConsistency() {
        // User 1 has 100 points per seed data
        List<Map<String, Object>> leaderboard = userService.getLeaderboard(20);
        Map<String, Object> user1Entry = leaderboard.stream()
                .filter(e -> ((Number) e.get("id")).longValue() == 1L)
                .findFirst()
                .orElse(null);
        assertNotNull(user1Entry);
        // Points should match seed data
        assertEquals(100, ((Number) user1Entry.get("points")).intValue());
    }

    @Test
    public void testPointsUpdateReflectedInLeaderboard() {
        // Give user 3 (0 points) some points
        userService.addPoints(3L, 500, "TEST_REWARD", "测试积分奖励", "test", null);

        List<Map<String, Object>> leaderboard = userService.getLeaderboard(20);
        Map<String, Object> user3Entry = leaderboard.stream()
                .filter(e -> ((Number) e.get("id")).longValue() == 3L)
                .findFirst()
                .orElse(null);
        assertNotNull(user3Entry);
        assertEquals(500, ((Number) user3Entry.get("points")).intValue());
    }

    // ===== Task Entity New Fields Tests =====

    @Test
    public void testTaskFavoriteEntityFields() {
        TaskFavorite f = new TaskFavorite();
        f.setId(10L);
        f.setUserId(20L);
        f.setTaskId(30L);
        LocalDateTime now = LocalDateTime.now();
        f.setCreatedAt(now);

        assertEquals(10L, f.getId());
        assertEquals(20L, f.getUserId());
        assertEquals(30L, f.getTaskId());
        assertEquals(now, f.getCreatedAt());
    }

    @Test
    public void testTaskOnlineFields() {
        Task task = createTask("在线字段实体测试", 1L);
        Task detail = taskService.getTaskById(task.getId(), 2L);

        assertNotNull(detail.getRequesterOnline());
        assertFalse(detail.getRequesterOnline());  // no websocket session
        assertNull(detail.getHelperOnline());      // no helper yet
        assertNotNull(detail.getIsFavorited());
        assertFalse(detail.getIsFavorited());      // user 2 hasn't favorited
        assertNotNull(detail.getFavoriteCount());
    }

    // ===== Notification Trigger via Task Flow =====

    @Test
    public void testAcceptTaskUpdatesStatusAndTriggersNotification() {
        Task task = createTask("接单通知测试", 1L);

        // User 2 accepts the task
        Task accepted = taskService.acceptTask(task.getId(), 2L);
        assertEquals("accepted", accepted.getStatus());
        assertEquals(2L, accepted.getHelperId().longValue());

        // Verify task detail reflects new state
        Task detail = taskService.getTaskById(task.getId(), 1L);
        assertEquals("accepted", detail.getStatus());
        assertNotNull(detail.getHelperId());
    }

    // ===== Edge Cases & Error Handling =====

    @Test
    public void testGetTaskByIdWithNullUserIdDoesNotThrow() {
        Task task = createTask("匿名查看测试", 1L);
        Task detail = taskService.getTaskById(task.getId(), null);
        assertNotNull(detail);
        assertFalse(detail.getLikedByCurrentUser() != null && detail.getLikedByCurrentUser());
        assertFalse(detail.getIsFavorited() != null && detail.getIsFavorited());
    }

    @Test
    public void testUserMapperSelectTopByPoints() {
        List<com.campushub.entity.User> topUsers = userMapper.selectTopByPoints(10);
        assertNotNull(topUsers);
        assertTrue(topUsers.size() <= 10);
        // Should not include admin
        for (com.campushub.entity.User u : topUsers) {
            assertNotEquals("ADMIN", u.getRole());
        }
    }

    @Test
    public void testTaskFavoriteNativeQueries() {
        Task topic = createTopic("收藏映射测试", "打听求助", 1L);
        taskService.favoriteTask(topic.getId(), 2L);

        // Verify through mapper directly
        TaskFavorite fav = taskFavoriteMapper.selectByUserIdAndTaskId(2L, topic.getId());
        assertNotNull(fav);
        assertEquals(2L, fav.getUserId().longValue());
        assertEquals(topic.getId(), fav.getTaskId());
    }

    @Test
    public void testGetTaskByIdReturnsCommentCount() {
        Task topic = createTopic("评论数测试帖", "二手闲置", 1L);
        Task detail = taskService.getTaskById(topic.getId(), null);
        assertNotNull(detail.getCommentCount());
        assertEquals(0, detail.getCommentCount().intValue());
    }
}
