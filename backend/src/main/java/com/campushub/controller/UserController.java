package com.campushub.controller;

import com.campushub.dto.UserVO;
import com.campushub.entity.User;
import com.campushub.entity.UserPointRecord;
import com.campushub.entity.UserSetting;
import com.campushub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserVO getCurrentUser(Authentication authentication) {
        return UserVO.from(userService.getUserById(getCurrentUserId(authentication)));
    }

    @GetMapping("/me/points/records")
    public List<UserPointRecord> getCurrentUserPointRecords(Authentication authentication) {
        return userService.getPointRecords(getCurrentUserId(authentication));
    }

    @PutMapping("/me")
    public UserVO updateCurrentUser(@RequestBody User user, Authentication authentication) {
        return UserVO.from(userService.updateUser(getCurrentUserId(authentication), user));
    }

    @GetMapping("/{id}")
    public UserVO getUserById(@PathVariable Long id) {
        return UserVO.from(userService.getUserById(id));
    }

    @GetMapping("/settings")
    public UserSetting getUserSetting(Authentication authentication) {
        return userService.getUserSetting(getCurrentUserId(authentication));
    }

    @PutMapping("/settings")
    public UserSetting updateUserSetting(@RequestBody UserSetting userSetting, Authentication authentication) {
        return userService.updateUserSetting(getCurrentUserId(authentication), userSetting);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}
