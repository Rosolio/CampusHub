package com.campushub.mapper;

import com.campushub.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    Notification selectById(Long id);
    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    int countUnreadByUserId(Long userId);
    int insert(Notification notification);
    int updateReadStatus(@Param("id") Long id, @Param("isRead") Boolean isRead);
    int markAllReadByUserId(Long userId);
}
