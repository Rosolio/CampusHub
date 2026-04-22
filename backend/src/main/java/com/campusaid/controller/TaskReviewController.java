package com.campusaid.controller;

import com.campusaid.dto.TaskReviewCreateRequest;
import com.campusaid.entity.TaskReview;
import com.campusaid.service.TaskReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks/{taskId}/reviews")
public class TaskReviewController {

    private final TaskReviewService taskReviewService;

    public TaskReviewController(TaskReviewService taskReviewService) {
        this.taskReviewService = taskReviewService;
    }

    @GetMapping
    public List<TaskReview> getTaskReviews(@PathVariable Long taskId) {
        return taskReviewService.getTaskReviews(taskId);
    }

    @PostMapping
    public TaskReview createTaskReview(
        @PathVariable Long taskId,
        @RequestBody TaskReviewCreateRequest request,
        Authentication authentication
    ) {
        return taskReviewService.createTaskReview(taskId, request, Long.parseLong(authentication.getName()));
    }
}
