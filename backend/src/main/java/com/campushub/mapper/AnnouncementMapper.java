package com.campushub.mapper;

import com.campushub.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AnnouncementMapper {
    List<Announcement> selectAll();
    Announcement selectById(Long id);
    int insert(Announcement announcement);
    int update(Announcement announcement);
}
