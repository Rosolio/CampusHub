package com.campusaid.controller;

import com.campusaid.dto.TaskCommentCreateRequest;
import com.campusaid.entity.TaskComment;
import com.campusaid.service.TaskCommentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @GetMapping
    public List<TaskComment> getComments(@PathVariable Long taskId, Authentication authentication) {
        return taskCommentService.getCommentsByTaskId(taskId, Long.parseLong(authentication.getName()));
    }

    @PostMapping
    public TaskComment createComment(
        @PathVariable Long taskId,
        @RequestBody TaskCommentCreateRequest request,
        Authentication authentication
    ) {
        return taskCommentService.createComment(taskId, request, Long.parseLong(authentication.getName()));
    }

    @PostMapping("/{commentId}/like")
    public TaskComment likeComment(
        @PathVariable Long taskId,
        @PathVariable Long commentId,
        Authentication authentication
    ) {
        return taskCommentService.likeComment(taskId, commentId, Long.parseLong(authentication.getName()));
    }

    @DeleteMapping("/{commentId}/like")
    public TaskComment unlikeComment(
        @PathVariable Long taskId,
        @PathVariable Long commentId,
        Authentication authentication
    ) {
        return taskCommentService.unlikeComment(taskId, commentId, Long.parseLong(authentication.getName()));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
        @PathVariable Long taskId,
        @PathVariable Long commentId,
        Authentication authentication
    ) {
        taskCommentService.deleteComment(taskId, commentId, Long.parseLong(authentication.getName()));
    }
}
