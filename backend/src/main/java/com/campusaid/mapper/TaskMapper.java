package com.campusaid.mapper;

import com.campusaid.entity.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskMapper {
    Task selectById(Long id);
    List<Task> selectAll();
    List<Task> selectByRequesterId(Long requesterId);
    List<Task> selectByHelperId(Long helperId);
    List<Task> selectByStatus(String status);
    Integer sumTopicLikesByRequesterId(Long requesterId);
    int insert(Task task);
    int update(Task task);
    int delete(Long id);
}
