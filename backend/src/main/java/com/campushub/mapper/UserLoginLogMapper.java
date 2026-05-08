package com.campushub.mapper;

import com.campushub.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserLoginLogMapper {
    int insert(UserLoginLog userLoginLog);
    Integer countDistinctUsersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    List<Map<String, Object>> countDistinctUsersByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
