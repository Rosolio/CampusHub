package com.campushub.util;

import com.campushub.entity.Task;

import java.util.Map;
import java.util.Set;

public final class TaskModeResolver {
    private static final String ERRAND_CATEGORY = "跑腿代办";
    private static final Set<String> TASK_CATEGORIES = Set.of(ERRAND_CATEGORY, "学习辅导");
    private static final Map<String, String> TYPE_MAP = Map.ofEntries(
        Map.entry("errand", ERRAND_CATEGORY),
        Map.entry("study", "学习辅导"),
        Map.entry("secondhand", "二手闲置"),
        Map.entry("help", "打听求助"),
        Map.entry("social", "恋爱交友"),
        Map.entry("job", "兼职招聘"),
        Map.entry("校园配送", ERRAND_CATEGORY),
        Map.entry("学业辅导", "学习辅导"),
        Map.entry("闲置交换", "二手闲置"),
        Map.entry("信息求助", "打听求助"),
        Map.entry("社交互助", "恋爱交友"),
        Map.entry("兼职机会", "兼职招聘")
    );

    private TaskModeResolver() {
    }

    public static Task normalize(Task task) {
        if (task == null) {
            return null;
        }

        String category = resolveCategory(task);
        if (isBlank(task.getCategory()) && category != null) {
            task.setCategory(category);
        }

        String resolvedTaskMode = TASK_CATEGORIES.contains(category) ? "task" : "topic";
        if (isBlank(task.getTaskMode()) || !resolvedTaskMode.equals(task.getTaskMode())) {
            task.setTaskMode(resolvedTaskMode);
        }

        return task;
    }

    public static boolean isTopicTask(Task task) {
        Task normalizedTask = normalize(task);
        return normalizedTask != null && "topic".equals(normalizedTask.getTaskMode());
    }

    public static boolean isTask(Task task) {
        Task normalizedTask = normalize(task);
        return normalizedTask != null && "task".equals(normalizedTask.getTaskMode());
    }

    private static String resolveCategory(Task task) {
        if (task == null) {
            return ERRAND_CATEGORY;
        }
        if (!isBlank(task.getCategory())) {
            return task.getCategory();
        }

        String mappedCategory = firstMappedValue(task.getImpactText(), task.getBadgeSecondary());
        return mappedCategory != null ? mappedCategory : ERRAND_CATEGORY;
    }

    private static String firstMappedValue(String... candidates) {
        for (String candidate : candidates) {
            if (!isBlank(candidate) && TYPE_MAP.containsKey(candidate)) {
                return TYPE_MAP.get(candidate);
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
