package com.campushub.test;

import com.campushub.entity.User;
import com.campushub.entity.UserPointRecord;
import com.campushub.entity.UserSetting;
import com.campushub.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class UserTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    @Test
    public void testUpdateProfileAndReFetchReturnsUpdatedData() {
        User updateReq = new User();
        updateReq.setName("新昵称");
        updateReq.setEmail("new@example.com");
        updateReq.setMajor("人工智能");
        updateReq.setAvatarUrl("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA");

        User updated = userService.updateUser(1L, updateReq);
        assertNotNull(updated);
        assertEquals("新昵称", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("人工智能", updated.getMajor());
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA", updated.getAvatarUrl());

        User refetched = userService.getUserById(1L);
        assertNotNull(refetched);
        assertEquals("新昵称", refetched.getName(), "Re-fetch should return updated name");
        assertEquals("new@example.com", refetched.getEmail(), "Re-fetch should return updated email");
        assertEquals("人工智能", refetched.getMajor(), "Re-fetch should return updated major");
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA", refetched.getAvatarUrl(), "Re-fetch should return updated avatar");
    }

    @Test
    public void testUpdateNameOnlyDoesNotClearOtherFields() {
        User updateReq = new User();
        updateReq.setName("仅修改昵称");

        User updated = userService.updateUser(1L, updateReq);
        assertEquals("仅修改昵称", updated.getName());

        User refetched = userService.getUserById(1L);
        assertEquals("仅修改昵称", refetched.getName());
        assertNotNull(refetched.getEmail(), "Email should not be cleared");
        assertNotNull(refetched.getStudentId(), "StudentId should not be cleared");
        assertNotNull(refetched.getPassword(), "Password should not be cleared");
    }

    @Test
    public void testUpdateAvatarUrlPersists() {
        User updateReq = new User();
        updateReq.setAvatarUrl("data:image/jpeg;base64,/9j/4AAQSkZJRg==");

        User updated = userService.updateUser(1L, updateReq);
        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRg==", updated.getAvatarUrl());

        // Clear cache to force re-read from DB
        redisTemplate.delete("users:1");

        User refetched = userService.getUserById(1L);
        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRg==", refetched.getAvatarUrl());
    }
}
