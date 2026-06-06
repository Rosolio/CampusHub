package com.campushub.mapper;

import com.campushub.entity.Task;
import com.campushub.entity.TaskFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskFavoriteMapper {
    TaskFavorite selectByUserIdAndTaskId(@Param("userId") Long userId, @Param("taskId") Long taskId);
    List<Task> selectFavoriteTasksByUserId(@Param("userId") Long userId);
    int insert(TaskFavorite taskFavorite);
    int deleteByUserIdAndTaskId(@Param("userId") Long userId, @Param("taskId") Long taskId);
}
