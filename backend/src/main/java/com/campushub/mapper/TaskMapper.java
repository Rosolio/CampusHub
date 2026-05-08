package com.campushub.mapper;

import com.campushub.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {
    Task selectById(Long id);
    List<Task> selectAll();
    List<Task> selectByRequesterId(Long requesterId);
    List<Task> selectByHelperId(Long helperId);
    List<Task> selectByStatus(String status);
    List<Task> selectAdminTasks();
    Integer sumTopicLikesByRequesterId(Long requesterId);
    List<Map<String, Object>> countTasksByReviewStatus();
    List<Map<String, Object>> countTasksByCategory();
    List<Map<String, Object>> countCompletedTasksByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    int insert(Task task);
    int update(Task task);
    int delete(Long id);
}
