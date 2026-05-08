package com.campushub.mapper;

import com.campushub.entity.UserSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSettingMapper {
    UserSetting selectById(Long id);
    UserSetting selectByUserId(Long userId);
    int insert(UserSetting userSetting);
    int update(UserSetting userSetting);
    int delete(Long id);
}
