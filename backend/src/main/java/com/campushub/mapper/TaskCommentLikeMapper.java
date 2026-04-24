package com.campushub.mapper;

import com.campushub.entity.TaskCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskCommentLikeMapper {
    TaskCommentLike selectByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    int insert(TaskCommentLike taskCommentLike);
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    int deleteByCommentIds(@Param("commentIds") List<Long> commentIds);
    int deleteByTaskId(Long taskId);
}
