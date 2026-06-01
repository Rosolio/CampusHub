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

    /** Paginated feed query with filters pushed to SQL */
    List<Task> selectFeedTasks(@Param("query") com.campushub.dto.TaskRecommendationQuery query,
                               @Param("now") LocalDateTime now);

    /** Count matching feed tasks (for pagination metadata) */
    int countFeedTasks(@Param("query") com.campushub.dto.TaskRecommendationQuery query,
                       @Param("now") LocalDateTime now);

    /** Lightweight category history query (no comment count, no helper join) */
    List<Task> selectCategoryHistoryByHelperId(@Param("helperId") Long helperId);

    Integer sumTopicLikesByRequesterId(Long requesterId);
    List<Map<String, Object>> countTasksByReviewStatus();
    List<Map<String, Object>> countTasksByCategory();
    List<Map<String, Object>> countCompletedTasksByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    int insert(Task task);
    int update(Task task);
    int delete(Long id);
}
