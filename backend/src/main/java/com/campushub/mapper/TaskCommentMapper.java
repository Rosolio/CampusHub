package com.campushub.mapper;

import com.campushub.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskCommentMapper {
    List<TaskComment> selectByTaskId(Long taskId);
    TaskComment selectById(Long id);
    List<TaskComment> selectByParentId(Long parentId);
    Integer sumLikesByAuthorId(Long authorId);
    int insert(TaskComment comment);
    int update(TaskComment comment);
    int deleteByIds(@Param("ids") List<Long> ids);
    int deleteByTaskId(Long taskId);
}
