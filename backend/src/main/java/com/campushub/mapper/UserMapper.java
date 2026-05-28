package com.campushub.mapper;

import com.campushub.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    User selectById(Long id);
    String selectRoleById(Long id);
    User selectByStudentId(String studentId);
    User selectByLoginIdentifier(String identifier);
    User selectByEmail(String email);
    List<User> selectAll();
    List<User> selectAdminUsers();
    List<Map<String, Object>> countUsersByStatus();
    int insert(User user);
    int update(User user);
    int incrementPoints(@Param("id") Long id, @Param("points") int points);
    int updateScore(@Param("id") Long id, @Param("score") BigDecimal score);
    int updateUserStatus(@Param("id") Long id, @Param("status") String status, @Param("disabledReason") String disabledReason);
    int updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);
    int updateVerifiedStatus(@Param("id") Long id, @Param("verifiedStatus") String verifiedStatus);
    int delete(Long id);
}
