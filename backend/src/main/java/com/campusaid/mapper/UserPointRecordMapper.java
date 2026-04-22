package com.campusaid.mapper;

import com.campusaid.entity.UserPointRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserPointRecordMapper {
    int insert(UserPointRecord record);
    List<UserPointRecord> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    Integer sumPositivePointsByUserIdAndChangeTypeBetween(
        @Param("userId") Long userId,
        @Param("changeType") String changeType,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
