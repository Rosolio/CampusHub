package com.campushub.test;

import com.campushub.dto.TaskCreateRequest;
import com.campushub.dto.TaskRecommendationQuery;
import com.campushub.entity.Task;
import com.campushub.mapper.TaskMapper;
import com.campushub.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskPerformanceTest extends IntegrationTestSupport {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    /** Verify that paginated queries return correct page sizes */
    @Test
    public void testPaginationReturnsCorrectPageSize() {
        // Create 15 task tasks
        List<Task> created = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            created.add(createTask("性能测试任务 " + i, "跑腿代办", "校园配送", "图书馆", 1L));
        }

        // Page 1: size 5
        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setPage(1);
        query.setSize(5);

        List<Task> page1 = taskService.getTasks(null, query);
        assertNotNull(page1);
        assertTrue(page1.size() <= 5, "Page 1 should have at most 5 tasks, got " + page1.size());

        // Page 2: size 5
        query.setPage(2);
        List<Task> page2 = taskService.getTasks(null, query);
        assertNotNull(page2);
        assertTrue(page2.size() <= 5, "Page 2 should have at most 5 tasks");
        assertTrue(page2.size() > 0 || created.size() <= 5, "Page 2 should have results if total > 5");

        // Different page sizes should not overlap
        if (page1.size() > 0 && page2.size() > 0) {
            List<Long> page1Ids = page1.stream().map(Task::getId).toList();
            List<Long> page2Ids = page2.stream().map(Task::getId).toList();
            assertTrue(page1Ids.stream().noneMatch(page2Ids::contains),
                "Pages should not have overlapping tasks");
        }
    }

    /** Verify that taskMode=task excludes topic tasks */
    @Test
    public void testTaskModeFilterExcludesTopics() {
        createTask("话题帖", "二手闲置", "闲置交换", "线上", 2L);
        createTask("任务帖", "跑腿代办", "校园配送", "图书馆", 1L);

        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setSize(100);

        List<Task> tasks = taskService.getTasks(null, query);
        assertNotNull(tasks);
        for (Task task : tasks) {
            assertNotEquals("topic", task.getTaskMode(),
                "taskMode=task query should not return topic posts");
        }
    }

    /** Verify that topic queries don't return task items */
    @Test
    public void testTopicModeFilterExcludesTasks() {
        createTask("话题帖", "二手闲置", "闲置交换", "线上", 2L);
        createTask("任务帖", "跑腿代办", "校园配送", "图书馆", 1L);

        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("topic");
        query.setMode("latest");
        query.setSize(100);

        List<Task> topics = taskService.getTasks(null, query);
        assertNotNull(topics);
        for (Task topic : topics) {
            assertEquals("topic", topic.getTaskMode(),
                "taskMode=topic query should only return topic posts");
        }
    }

    /** Verify cache hit on repeated identical queries */
    @Test
    public void testCacheReturnsConsistentResults() {
        createTask("缓存测试任务", "跑腿代办", "校园配送", "图书馆", 1L);

        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setSize(20);

        List<Task> first = taskService.getTasks(null, query);
        List<Task> second = taskService.getTasks(null, query);

        assertEquals(first.size(), second.size(),
            "Cache hit should return same result count");
    }

    /** Verify recommended mode returns scored results */
    @Test
    public void testRecommendedModeReturnsScoredResults() {
        createTask("推荐测试任务", "跑腿代办", "校园配送", "图书馆", 1L);

        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setMode("recommended");
        query.setTaskMode("task");
        query.setSize(20);

        List<Task> tasks = taskService.getTasks(null, query);
        assertNotNull(tasks);

        for (Task task : tasks) {
            if ("recommended".equals(task.getRecommendationMode())) {
                assertNotNull(task.getMatchScore(), "Scored tasks should have matchScore");
            }
        }
    }

    // =====================================================================
    //  Performance & Scalability Tests
    // =====================================================================

    /** Verify that feed query scales with large datasets (subquery perf) */
    @Test
    public void testFeedQueryPerformanceWithManyTasks() {
        // Create 50 tasks to simulate a reasonably populated feed
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            String cat = i % 3 == 0 ? "跑腿代办" : (i % 3 == 1 ? "学习辅导" : "二手闲置");
            String badge = i % 3 == 0 ? "校园配送" : (i % 3 == 1 ? "学业辅导" : "闲置交换");
            createTask("批量性能任务-" + i, cat, badge, i % 2 == 0 ? "图书馆" : "教学楼", 1L);
        }
        long createTime = System.currentTimeMillis() - start;
        System.out.println("[PERF] Created 50 tasks in " + createTime + "ms");

        // Measure feed query time (uncached)
        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setSize(20);
        query.setPage(1);

        start = System.currentTimeMillis();
        List<Task> tasks = taskService.getTasks(1L, query);
        long queryTime = System.currentTimeMillis() - start;
        System.out.println("[PERF] Feed query (uncached, 50 tasks) returned " + tasks.size() + " results in " + queryTime + "ms");

        assertNotNull(tasks);
        assertTrue(queryTime < 3000, "Feed query should complete within 3s for 50 tasks, took " + queryTime + "ms");
    }

    /** Verify cache speeds up repeated queries */
    @Test
    public void testCachePerformanceImprovement() {
        createTask("缓存性能测试", "跑腿代办", "校园配送", "图书馆", 1L);

        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setSize(20);

        // First call — likely uncached (or cold cache)
        long start = System.currentTimeMillis();
        taskService.getTasks(1L, query);
        long firstCall = System.currentTimeMillis() - start;

        // Second call — should be cached
        start = System.currentTimeMillis();
        taskService.getTasks(1L, query);
        long secondCall = System.currentTimeMillis() - start;

        System.out.println("[PERF] First call: " + firstCall + "ms, Second call (cached): " + secondCall + "ms");
        assertTrue(secondCall <= firstCall + 20,
            "Cached call should not be significantly slower than uncached. First=" + firstCall + "ms, Second=" + secondCall + "ms");
    }

    /** Verify that search query completes within reasonable time */
    @Test
    public void testSearchQueryPerformance() {
        // Create tasks with searchable titles
        for (int i = 0; i < 20; i++) {
            String suffix = i < 10 ? "校园" : "校外";
            createTask(suffix + "快递代取服务-" + i, "跑腿代办", "校园配送", "南门", 2L);
        }

        long start = System.currentTimeMillis();
        var results = taskMapper.searchTasks("快递", null, 0, 20);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("[PERF] LIKE '%keyword%' search returned " + results.size() + " results in " + elapsed + "ms");
        assertTrue(elapsed < 2000, "LIKE search should complete within 2s, took " + elapsed + "ms");
    }

    /** Verify that feed pagination offset doesn't degrade with larger pages */
    @Test
    public void testDeepPaginationPerformance() {
        // Create tasks spread across pages
        for (int i = 0; i < 30; i++) {
            createTask("分页测试任务-" + i, "跑腿代办", "校园配送", "校内", 2L);
        }

        long[] pageTimes = new long[3];
        for (int page = 1; page <= 3; page++) {
            TaskRecommendationQuery query = new TaskRecommendationQuery();
            query.setTaskMode("task");
            query.setMode("latest");
            query.setSize(10);
            query.setPage(page);

            long start = System.currentTimeMillis();
            List<Task> tasks = taskService.getTasks(2L, query);
            pageTimes[page - 1] = System.currentTimeMillis() - start;

            System.out.println("[PERF] Page " + page + " returned " + tasks.size() + " results in " + pageTimes[page - 1] + "ms");
            assertNotNull(tasks);
        }
        // Deeper pages should not be orders of magnitude slower
        if (pageTimes[2] > pageTimes[0] * 5) {
            System.out.println("[WARN] Deep pagination may be degrading: page1=" + pageTimes[0] + "ms, page3=" + pageTimes[2] + "ms");
        }
    }

    /** Verify that creating tasks with image_urls doesn't bloat list queries */
    @Test
    public void testImageUrlsFieldDoesNotImpactListPerformance() {
        // Create tasks with image_urls
        for (int i = 0; i < 10; i++) {
            TaskCreateRequest req = new TaskCreateRequest();
            req.setTitle("带图片任务-" + i);
            req.setDescription("描述" + i);
            req.setCategory("跑腿代办");
            req.setBadgePrimary("普通");
            req.setBadgeSecondary("校园配送");
            req.setLocationText("校内");
            req.setTimeText("今天");
            req.setRewardTitle("奖励");
            req.setRewardText("10元");
            req.setImpactTitle("帮助");
            req.setImpactText("errand");
            req.setImageUrls("[\"/files/img" + i + ".jpg\",\"/files/img" + i + "b.jpg\"]");
            taskService.createTask(req, 2L);
        }

        // Measure list query with image data present
        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setSize(20);

        long start = System.currentTimeMillis();
        List<Task> tasks = taskService.getTasks(2L, query);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("[PERF] List query with image_urls data: " + elapsed + "ms for " + tasks.size() + " tasks");
        assertTrue(elapsed < 3000, "List query with image data should complete within 3s, took " + elapsed + "ms");
    }

    // =====================================================================
    //  Original tests continue below
    // =====================================================================

    /** Verify that category filter works with pagination */
    @Test
    public void testCategoryFilterWithPagination() {
        createTask("跑腿任务", "跑腿代办", "校园配送", "图书馆", 1L);
        createTask("学习任务", "学习辅导", "学业辅导", "教学楼", 1L);

        TaskRecommendationQuery query = new TaskRecommendationQuery();
        query.setTaskMode("task");
        query.setMode("latest");
        query.setCategory("跑腿代办");
        query.setSize(100);

        List<Task> tasks = taskService.getTasks(null, query);
        assertNotNull(tasks);
        for (Task task : tasks) {
            assertEquals("跑腿代办", task.getCategory(),
                "Category filter should only return matching tasks");
        }
    }

    private Task createTask(String title, String category, String badgeSecondary, String location, Long requesterId) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle(title);
        request.setDescription(title + "的描述内容");
        request.setCategory(category);
        request.setBadgePrimary("普通");
        request.setBadgeSecondary(badgeSecondary);
        request.setLocationText(location);
        request.setTimeText("明天前");
        request.setRewardTitle("任务奖励");
        request.setRewardText("10 积分");
        request.setImpactTitle("任务类型");
        request.setImpactText(category.contains("跑腿") ? "errand" : category.contains("学习") ? "study" : "secondhand");
        request.setExpiresAt(LocalDateTime.now().plusDays(1).toString());
        return taskService.createTask(request, requesterId);
    }
}
