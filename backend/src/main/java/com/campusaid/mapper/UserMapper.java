package com.campusaid.mapper;

import com.campusaid.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {
    User selectById(Long id);
    User selectByStudentId(String studentId);
    User selectByEmail(String email);
    List<User> selectAll();
    int insert(User user);
    int update(User user);
    int incrementPoints(@Param("id") Long id, @Param("points") int points);
    int updateScore(@Param("id") Long id, @Param("score") BigDecimal score);
    int delete(Long id);
}
