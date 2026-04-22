package com.campusaid.mapper;

import com.campusaid.entity.TaskReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TaskReviewMapper {
    TaskReview selectById(Long id);
    List<TaskReview> selectByTaskId(Long taskId);
    TaskReview selectByTaskIdAndReviewerId(@Param("taskId") Long taskId, @Param("reviewerId") Long reviewerId);
    BigDecimal selectAverageRatingByRevieweeId(Long revieweeId);
    int insert(TaskReview taskReview);
    int deleteByTaskId(Long taskId);
}
