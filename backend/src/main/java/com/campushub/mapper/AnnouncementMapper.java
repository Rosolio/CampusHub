package com.campushub.mapper;

import com.campushub.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {
    List<Announcement> selectAll();
    Announcement selectById(Long id);
    int insert(Announcement announcement);
    int update(Announcement announcement);
    int deleteOlderThanDays(@Param("days") int days);
    int deleteById(@Param("id") Long id);
}
