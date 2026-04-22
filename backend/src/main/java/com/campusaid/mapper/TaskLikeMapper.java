package com.campusaid.mapper;

import com.campusaid.entity.TaskLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskLikeMapper {
    TaskLike selectByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    int insert(TaskLike taskLike);
    int deleteByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    int deleteByTaskId(Long taskId);
}
