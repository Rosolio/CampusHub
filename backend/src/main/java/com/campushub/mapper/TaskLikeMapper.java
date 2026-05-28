package com.campushub.mapper;

import com.campushub.entity.TaskLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface TaskLikeMapper {
    TaskLike selectByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    Set<Long> selectLikedTaskIdsByUserIdAndTaskIds(@Param("userId") Long userId, @Param("taskIds") List<Long> taskIds);
    int insert(TaskLike taskLike);
    int deleteByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    int deleteByTaskId(Long taskId);
}
