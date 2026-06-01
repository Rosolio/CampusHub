package com.campushub.service;

import com.campushub.dto.TaskRecommendationQuery;
import com.campushub.entity.Task;
import com.campushub.mapper.TaskMapper;
import com.campushub.util.TaskModeResolver;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskRecommendationService {
    private static final String MODE_LATEST = "latest";
    private static final String MODE_RECOMMENDED = "recommended";
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 200;

    private final TaskMapper taskMapper;
    private final UserService userService;

    public TaskRecommendationService(TaskMapper taskMapper, UserService userService) {
        this.taskMapper = taskMapper;
        this.userService = userService;
    }

    public List<Task> applyRecommendation(List<Task> sourceTasks, TaskRecommendationQuery query, Long currentUserId) {
        TaskRecommendationQuery normalizedQuery = query == null ? new TaskRecommendationQuery() : query;
        String mode = normalizeMode(normalizedQuery.getMode());
        Map<String, Long> categoryHistory = loadCategoryHistory(currentUserId);
        long totalHistory = categoryHistory.values().stream().mapToLong(Long::longValue).sum();
        LocalDateTime availableAt = parseAvailableAt(normalizedQuery.getAvailableAt());
        LocalDateTime now = LocalDateTime.now();

        // Score and sort all task-mode candidates (limit applied by caller via pagination)
        List<Task> rankedTasks = sourceTasks.stream()
            .map(TaskModeResolver::normalize)
            .filter(task -> isVisibleCandidate(task, normalizedQuery, currentUserId, now, mode))
            .peek(task -> applyRecommendationFields(task, normalizedQuery, mode, categoryHistory, totalHistory, availableAt, now))
            .sorted(comparatorFor(mode))
            .collect(Collectors.toList());

        // Add topic tasks as-is (only relevant when taskMode is not pre-filtered)
        List<Task> untouchedTopics = sourceTasks.stream()
            .map(TaskModeResolver::normalize)
            .filter(TaskModeResolver::isTopicTask)
            .filter(this::isApproved)
            .peek(this::clearRecommendationFields)
            .collect(Collectors.toList());

        List<Task> result = new ArrayList<>(rankedTasks);
        result.addAll(untouchedTopics);
        return result;
    }

    private boolean isVisibleCandidate(Task task, TaskRecommendationQuery query, Long currentUserId, LocalDateTime now, String mode) {
        if (task == null || !isApproved(task) || !TaskModeResolver.isTask(task)) {
            return false;
        }
        if (MODE_RECOMMENDED.equals(mode) && currentUserId != null && currentUserId.equals(task.getRequesterId())) {
            return false;
        }
        if ("canceled".equals(task.getStatus()) || "completed".equals(task.getStatus())) {
            return false;
        }
        if (task.getExpiresAt() != null && task.getExpiresAt().isBefore(now)) {
            return false;
        }
        String category = normalizeText(query.getCategory());
        return category.isBlank() || category.equals(normalizeText(task.getCategory()));
    }

    private void applyRecommendationFields(
        Task task,
        TaskRecommendationQuery query,
        String mode,
        Map<String, Long> categoryHistory,
        long totalHistory,
        LocalDateTime availableAt,
        LocalDateTime now
    ) {
        if (MODE_LATEST.equals(mode)) {
            clearRecommendationFields(task);
            task.setRecommendationMode(MODE_LATEST);
            return;
        }

        List<String> reasons = new ArrayList<>();
        int score = 0;
        score += scoreCategory(task, query, categoryHistory, totalHistory, reasons);
        score += scoreLocation(task, query, reasons);
        score += scoreTime(task, availableAt, now, reasons);
        score += scoreHistory(task, categoryHistory, totalHistory, reasons);
        score += scoreFreshnessAndQuality(task, now);

        task.setMatchScore(Math.max(0, Math.min(100, score)));
        task.setMatchReasons(reasons.stream().limit(3).collect(Collectors.toList()));
        task.setMatchedCategory(task.getCategory());
        task.setRecommendationMode(MODE_RECOMMENDED);
    }

    private int scoreCategory(Task task, TaskRecommendationQuery query, Map<String, Long> categoryHistory, long totalHistory, List<String> reasons) {
        String selectedCategory = normalizeText(query.getCategory());
        String taskCategory = normalizeText(task.getCategory());
        if (!selectedCategory.isBlank()) {
            if (selectedCategory.equals(taskCategory)) {
                reasons.add("匹配筛选分类");
                return 35;
            }
            return 0;
        }
        if (totalHistory <= 0) {
            return 18;
        }
        long categoryCount = categoryHistory.getOrDefault(taskCategory, 0L);
        if (categoryCount <= 0) {
            return 8;
        }
        reasons.add("符合接单偏好");
        return (int) Math.round(35.0 * categoryCount / totalHistory);
    }

    private int scoreLocation(Task task, TaskRecommendationQuery query, List<String> reasons) {
        String userLocation = normalizeText(query.getLocation());
        String taskLocation = normalizeText(task.getLocationText());
        if (userLocation.isBlank() || taskLocation.isBlank()) {
            return 10;
        }
        if (taskLocation.contains(userLocation) || userLocation.contains(taskLocation)) {
            reasons.add("地点高度匹配");
            return 20;
        }
        String sharedZone = sharedCampusZone(userLocation, taskLocation);
        if (!sharedZone.isBlank()) {
            reasons.add("同属" + sharedZone);
            return 15;
        }
        return sharedTokenCount(userLocation, taskLocation) > 0 ? 12 : 4;
    }

    private int scoreTime(Task task, LocalDateTime availableAt, LocalDateTime now, List<String> reasons) {
        LocalDateTime deadline = task.getExpiresAt();
        if (deadline == null) {
            reasons.add("时间较灵活");
            return 12;
        }
        if (deadline.isBefore(now)) {
            return 0;
        }
        LocalDateTime referenceTime = availableAt == null ? now : availableAt;
        if (deadline.isBefore(referenceTime)) {
            return 4;
        }
        long hours = Math.max(0, Duration.between(referenceTime, deadline).toHours());
        if (hours <= 24) {
            reasons.add("近期可完成");
            return 20;
        }
        if (hours <= 72) {
            reasons.add("时间窗口合适");
            return 15;
        }
        return 10;
    }

    private int scoreHistory(Task task, Map<String, Long> categoryHistory, long totalHistory, List<String> reasons) {
        if (totalHistory <= 0) {
            return 0;
        }
        String taskCategory = normalizeText(task.getCategory());
        long categoryCount = categoryHistory.getOrDefault(taskCategory, 0L);
        if (categoryCount <= 0) {
            return 0;
        }
        if (categoryCount >= 2) {
            reasons.add("常接" + task.getCategory());
        }
        return (int) Math.round(20.0 * categoryCount / totalHistory);
    }

    private int scoreFreshnessAndQuality(Task task, LocalDateTime now) {
        int score = 0;
        if (task.getCreatedAt() != null) {
            long hours = Duration.between(task.getCreatedAt(), now).toHours();
            if (hours <= 24) {
                score += 3;
            } else if (hours <= 72) {
                score += 2;
            } else {
                score += 1;
            }
        }
        BigDecimal requesterScore = task.getRequesterScore();
        if (requesterScore != null && requesterScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            score += 2;
        } else if (requesterScore != null && requesterScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            score += 1;
        }
        return score;
    }

    private Map<String, Long> loadCategoryHistory(Long currentUserId) {
        if (currentUserId == null || userService.isAdmin(currentUserId)) {
            return Map.of();
        }
        Map<String, Long> history = new HashMap<>();
        // Use lightweight query — no comment count, no helper join, limited to 200 rows
        taskMapper.selectCategoryHistoryByHelperId(currentUserId).stream()
            .map(TaskModeResolver::normalize)
            .filter(task -> TaskModeResolver.isTask(task) && isHistoricalHelperTask(task))
            .forEach(task -> history.merge(normalizeText(task.getCategory()), 1L, Long::sum));
        return history;
    }

    private boolean isHistoricalHelperTask(Task task) {
        return task != null && ("completed".equals(task.getStatus()) || !"canceled".equals(task.getStatus()));
    }

    private Comparator<Task> comparatorFor(String mode) {
        if (MODE_LATEST.equals(mode)) {
            return Comparator
                .comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Task::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return Comparator
            .comparing((Task task) -> task.getMatchScore() == null ? 0 : task.getMatchScore(), Comparator.reverseOrder())
            .thenComparing((Task task) -> "pending".equals(task.getStatus()) ? 0 : 1)
            .thenComparing(Task::getExpiresAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Task::getId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private boolean isApproved(Task task) {
        return task != null && "approved".equalsIgnoreCase(task.getReviewStatus());
    }

    private void clearRecommendationFields(Task task) {
        task.setMatchScore(null);
        task.setMatchReasons(null);
        task.setMatchedCategory(null);
        task.setRecommendationMode(null);
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return MODE_RECOMMENDED;
        }
        return MODE_LATEST.equalsIgnoreCase(mode.trim()) ? MODE_LATEST : MODE_RECOMMENDED;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private LocalDateTime parseAvailableAt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String sharedCampusZone(String first, String second) {
        String[] zones = {"南", "北", "东", "西", "宿舍", "图书馆", "食堂", "教学楼", "快递", "操场"};
        for (String zone : zones) {
            if (first.contains(zone) && second.contains(zone)) {
                return zone;
            }
        }
        return "";
    }

    private int sharedTokenCount(String first, String second) {
        int count = 0;
        for (String token : first.split("\\s+")) {
            if (!token.isBlank() && second.contains(token)) {
                count++;
            }
        }
        return count;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
