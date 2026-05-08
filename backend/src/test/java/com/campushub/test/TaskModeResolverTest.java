package com.campushub.test;

import com.campushub.entity.Task;
import com.campushub.util.TaskModeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskModeResolverTest {

    @Test
    void shouldInferLegacyTopicFieldsFromBadgeSecondary() {
        Task task = new Task();
        task.setBadgeSecondary("信息求助");

        TaskModeResolver.normalize(task);

        assertEquals("打听求助", task.getCategory());
        assertEquals("topic", task.getTaskMode());
        assertTrue(TaskModeResolver.isTopicTask(task));
    }

    @Test
    void shouldFixConflictingLegacyTaskMode() {
        Task task = new Task();
        task.setCategory("打听求助");
        task.setTaskMode("task");

        TaskModeResolver.normalize(task);

        assertEquals("topic", task.getTaskMode());
        assertTrue(TaskModeResolver.isTopicTask(task));
    }

    @Test
    void shouldNormalizeStudyCategoryAsTask() {
        Task task = new Task();
        task.setImpactText("study");
        task.setBadgeSecondary("学业辅导");
        task.setTaskMode("topic");

        TaskModeResolver.normalize(task);

        assertEquals("学习辅导", task.getCategory());
        assertEquals("task", task.getTaskMode());
    }

    @ParameterizedTest
    @CsvSource({
        "secondhand,闲置交换,二手闲置",
        "social,社交互助,恋爱交友",
        "job,兼职机会,兼职招聘"
    })
    void shouldNormalizeAllTopicCategories(String impactText, String badgeSecondary, String category) {
        Task task = new Task();
        task.setImpactText(impactText);
        task.setBadgeSecondary(badgeSecondary);
        task.setTaskMode("task");

        TaskModeResolver.normalize(task);

        assertEquals(category, task.getCategory());
        assertEquals("topic", task.getTaskMode());
        assertTrue(TaskModeResolver.isTopicTask(task));
    }
}
