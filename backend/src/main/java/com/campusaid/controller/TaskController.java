package com.campusaid.controller;

import com.campusaid.dto.TaskCreateRequest;
import com.campusaid.entity.Task;
import com.campusaid.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks(Authentication authentication) {
        return taskService.getTasks(getCurrentUserId(authentication));
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

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}
