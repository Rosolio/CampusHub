package com.campushub.controller;

import com.campushub.entity.Task;
import com.campushub.mapper.TaskMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final TaskMapper taskMapper;

    public SearchController(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String mode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String keyword = q.trim();
        if (keyword.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("results", List.of());
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            return result;
        }

        String searchMode = ("task".equals(mode) || "topic".equals(mode)) ? mode : null;
        int offset = Math.max(0, (page - 1)) * size;

        List<Task> tasks = taskMapper.searchTasks(keyword, searchMode, offset, size);
        int total = taskMapper.countSearchTasks(keyword, searchMode);

        Map<String, Object> result = new HashMap<>();
        result.put("results", tasks);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }
}
