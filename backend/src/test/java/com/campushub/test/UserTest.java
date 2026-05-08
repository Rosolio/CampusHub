package com.campushub.test;

import com.campushub.entity.User;
import com.campushub.entity.UserPointRecord;
import com.campushub.entity.UserSetting;
import com.campushub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class UserTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Test
    public void testGetUserById() {
        User user = userService.getUserById(1L);
        assertNotNull(user);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setName("更新后的测试用户");
        user.setEmail("updated@example.com");

        User updatedUser = userService.updateUser(1L, user);
        assertNotNull(updatedUser);
        assertEquals("更新后的测试用户", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    public void testGetUserSetting() {
        UserSetting userSetting = userService.getUserSetting(1L);
        assertNotNull(userSetting);
    }

    @Test
    public void testUpdateUserSetting() {
        UserSetting userSetting = new UserSetting();
        userSetting.setNotificationEnabled(false);
        userSetting.setTheme("dark");
        userSetting.setLanguage("en-US");

        UserSetting updatedSetting = userService.updateUserSetting(1L, userSetting);
        assertNotNull(updatedSetting);
        assertFalse(updatedSetting.getNotificationEnabled());
        assertEquals("dark", updatedSetting.getTheme());
        assertEquals("en-US", updatedSetting.getLanguage());
    }

    @Test
    public void testAddPointsCreatesPointRecord() {
        userService.addPoints(1L, 6, "GENERAL", "测试积分明细", "manual", 1L);

        List<UserPointRecord> records = userService.getPointRecords(1L);
        assertFalse(records.isEmpty());
        assertEquals("测试积分明细", records.get(0).getDescription());
        assertEquals(6, records.get(0).getPoints());
    }

}
