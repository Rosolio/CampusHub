package com.campushub.mapper;

import com.campushub.entity.UserVerification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserVerificationMapper {
    UserVerification selectById(Long id);
    UserVerification selectByUserId(Long userId);
    List<UserVerification> selectAll();
    int insert(UserVerification verification);
    int update(UserVerification verification);
    int delete(Long id);
    int countPendingVerifications();
}
