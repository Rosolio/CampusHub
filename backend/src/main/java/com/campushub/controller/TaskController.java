package com.campushub.controller;

import com.campushub.dto.TaskCreateRequest;
import com.campushub.dto.TaskRecommendationQuery;
import com.campushub.entity.Task;
import com.campushub.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks(@ModelAttribute TaskRecommendationQuery query, Authentication authentication) {
        return taskService.getTasks(getCurrentUserId(authentication), query);
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id, Authentication authentication) {
        return taskService.getTaskById(id, getCurrentUserId(authentication));
    }

    @PostMapping
    public Task createTask(@RequestBody TaskCreateRequest request, Authentication authentication) {
        return taskService.createTask(request, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/accept")
    public Task acceptTask(@PathVariable Long id, Authentication authentication) {
        return taskService.acceptTask(id, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/unaccept")
    public Task unacceptTask(@PathVariable Long id, Authentication authentication) {
        return taskService.unacceptTask(id, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/like")
    public Task likeTask(@PathVariable Long id, Authentication authentication) {
        return taskService.likeTask(id, getCurrentUserId(authentication));
    }

    @DeleteMapping("/{id}/like")
    public Task unlikeTask(@PathVariable Long id, Authentication authentication) {
        return taskService.unlikeTask(id, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/complete")
    public Task completeTask(@PathVariable Long id, Authentication authentication) {
        return taskService.completeTask(id, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/cancel")
    public Task cancelTask(@PathVariable Long id, Authentication authentication) {
        return taskService.cancelTask(id, getCurrentUserId(authentication));
    }

    @GetMapping("/my")
    public List<Task> getMyTasks(Authentication authentication) {
        return taskService.getMyTasks(getCurrentUserId(authentication));
    }

    @GetMapping("/my/accepted")
    public List<Task> getMyAcceptedTasks(Authentication authentication) {
        return taskService.getMyAcceptedTasks(getCurrentUserId(authentication));
    }

    @GetMapping("/my/received-likes/count")
    public int getMyReceivedLikeCount(Authentication authentication) {
        return taskService.getMyReceivedLikeCount(getCurrentUserId(authentication));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/delete")
    public void deleteTaskCompat(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, getCurrentUserId(authentication));
    }

    @PostMapping("/{id}/favorite")
    public Map<String, Object> favoriteTask(@PathVariable Long id, Authentication authentication) {
        taskService.favoriteTask(id, getCurrentUserId(authentication));
        return Map.of("favorited", true);
    }

    @DeleteMapping("/{id}/favorite")
    public Map<String, Object> unfavoriteTask(@PathVariable Long id, Authentication authentication) {
        taskService.unfavoriteTask(id, getCurrentUserId(authentication));
        return Map.of("favorited", false);
    }

    @GetMapping("/favorites")
    public List<Task> getFavoriteTasks(Authentication authentication) {
        return taskService.getFavoriteTasks(getCurrentUserId(authentication));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}
