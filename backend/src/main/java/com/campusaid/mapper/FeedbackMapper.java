package com.campusaid.mapper;

import com.campusaid.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FeedbackMapper {
    Feedback selectById(Long id);
    List<Feedback> selectAll();
    List<Feedback> selectByUserId(Long userId);
    int insert(Feedback feedback);
    int update(Feedback feedback);
}
