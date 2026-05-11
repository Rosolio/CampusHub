package com.campushub.mapper;

import com.campushub.entity.TaskReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskReviewMapper {
    TaskReview selectById(Long id);
    List<TaskReview> selectByTaskId(Long taskId);
    List<Map<String, Object>> countByTaskIds(@Param("taskIds") List<Long> taskIds);
    TaskReview selectByTaskIdAndReviewerId(@Param("taskId") Long taskId, @Param("reviewerId") Long reviewerId);
    BigDecimal selectAverageRatingByRevieweeId(Long revieweeId);
    int insert(TaskReview taskReview);
    int deleteByTaskId(Long taskId);
}
