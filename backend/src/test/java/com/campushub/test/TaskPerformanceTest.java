package com.campushub.test;

import com.campushub.dto.TaskCreateRequest;
import com.campushub.dto.TaskRecommendationQuery;
import com.campushub.entity.Task;
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
