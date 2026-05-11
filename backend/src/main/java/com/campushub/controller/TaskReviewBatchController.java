package com.campushub.controller;

import com.campushub.service.TaskReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task-reviews")
public class TaskReviewBatchController {

    private final TaskReviewService taskReviewService;

    public TaskReviewBatchController(TaskReviewService taskReviewService) {
        this.taskReviewService = taskReviewService;
    }

    @GetMapping
    public Map<Long, Integer> getTaskReviewCounts(@RequestParam String taskIds) {
        List<Long> ids = Arrays.stream(taskIds.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(Long::parseLong)
            .collect(Collectors.toList());
        return taskReviewService.getTaskReviewCounts(ids);
    }
}
