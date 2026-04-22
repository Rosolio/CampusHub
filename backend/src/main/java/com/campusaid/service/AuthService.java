package com.campusaid.service;

import com.campusaid.dto.LoginRequest;
import com.campusaid.dto.RegisterRequest;
import com.campusaid.dto.ThirdPartyLoginRequest;
import com.campusaid.entity.User;
import com.campusaid.entity.UserSetting;
import com.campusaid.mapper.UserMapper;
import com.campusaid.mapper.UserSettingMapper;
import com.campusaid.util.JwtUtil;
import com.campusaid.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserSettingMapper userSettingMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, UserSettingMapper userSettingMapper, PasswordUtil passwordUtil, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.userSettingMapper = userSettingMapper;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectByStudentId(request.getStudentId());
        if (user == null || !passwordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("学号或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", user);
        return result;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        User existingByStudentId = userMapper.selectByStudentId(request.getStudentId());
        User existingByEmail = userMapper.selectByEmail(request.getEmail());
        if (existingByStudentId != null || existingByEmail != null) {
            User existingUser = existingByStudentId != null ? existingByStudentId : existingByEmail;
            boolean sameUser = existingByStudentId == null || existingByEmail == null
                    || existingByStudentId.getId().equals(existingByEmail.getId());
            if (sameUser && passwordUtil.matches(request.getPassword(), existingUser.getPassword())) {
                String token = jwtUtil.generateToken(existingUser.getId());
                String refreshToken = jwtUtil.generateRefreshToken(existingUser.getId());

                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("refreshToken", refreshToken);
                result.put("user", existingUser);
                return result;
            }

            if (existingByStudentId != null) {
                throw new RuntimeException("学号已注册");
            }
            throw new RuntimeException("邮箱已注册");
        }

        // 创建用户
        User user = new User();
        user.setStudentId(request.getStudentId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordUtil.encryptPassword(request.getPassword()));
        user.setScore(BigDecimal.ZERO);
        user.setPoints(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 创建用户设置
        UserSetting userSetting = new UserSetting();
        userSetting.setUserId(user.getId());
        userSetting.setNotificationEnabled(true);
        userSetting.setTheme("light");
        userSetting.setLanguage("zh-CN");
        userSetting.setUpdatedAt(LocalDateTime.now());
        userSettingMapper.insert(userSetting);

        String token = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", user);
        return result;
    }

    public String refreshToken(String refreshToken) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            return jwtUtil.generateToken(userId);
        } catch (Exception e) {
            throw new RuntimeException("刷新令牌无效");
        }
    }

    @Transactional
    public Map<String, Object> thirdPartyLogin(ThirdPartyLoginRequest request) {
        String provider = normalizeProvider(request.getProvider());
        String providerUserId = safeTrim(request.getProviderUserId());
        if (provider == null) {
            throw new RuntimeException("暂不支持该第三方登录方式");
        }
        if (providerUserId.isEmpty()) {
            throw new RuntimeException("第三方账号标识不能为空");
        }

        String syntheticStudentId = buildThirdPartyStudentId(provider, providerUserId);
        String syntheticEmail = buildThirdPartyEmail(provider, providerUserId, request.getEmail());
        User user = userMapper.selectByStudentId(syntheticStudentId);
        if (user == null) {
            user = userMapper.selectByEmail(syntheticEmail);
        }

        if (user == null) {
            user = new User();
            user.setStudentId(syntheticStudentId);
            user.setName(resolveThirdPartyDisplayName(provider, providerUserId, request.getDisplayName()));
            user.setEmail(syntheticEmail);
            user.setPassword(passwordUtil.encryptPassword(buildSyntheticPassword(provider, providerUserId)));
            user.setScore(BigDecimal.ZERO);
            user.setPoints(0);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);

            UserSetting userSetting = new UserSetting();
            userSetting.setUserId(user.getId());
            userSetting.setNotificationEnabled(true);
            userSetting.setTheme("light");
            userSetting.setLanguage("zh-CN");
            userSetting.setUpdatedAt(LocalDateTime.now());
            userSettingMapper.insert(userSetting);
        } else {
            boolean changed = false;
            String nextName = safeTrim(request.getDisplayName());
            if (!nextName.isEmpty() && !nextName.equals(user.getName())) {
                user.setName(nextName);
                changed = true;
            }
            String nextEmail = buildThirdPartyEmail(provider, providerUserId, request.getEmail());
            if (!nextEmail.equals(user.getEmail())) {
                user.setEmail(nextEmail);
                changed = true;
            }
            if (changed) {
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.update(user);
            }
        }

        String token = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", user);
        result.put("provider", provider);
        return result;
    }

    private String normalizeProvider(String provider) {
        String normalized = safeTrim(provider).toUpperCase(Locale.ROOT);
        if ("QQ".equals(normalized) || "SSO".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildThirdPartyStudentId(String provider, String providerUserId) {
        return "tp-" + provider.toLowerCase(Locale.ROOT) + "-" + shortToken(providerUserId);
    }

    private String buildThirdPartyEmail(String provider, String providerUserId, String rawEmail) {
        String email = safeTrim(rawEmail);
        if (!email.isEmpty()) {
            return email;
        }
        return provider.toLowerCase(Locale.ROOT) + "." + shortToken(providerUserId) + "@auth.campusaid.local";
    }

    private String resolveThirdPartyDisplayName(String provider, String providerUserId, String rawDisplayName) {
        String displayName = safeTrim(rawDisplayName);
        if (!displayName.isEmpty()) {
            return displayName;
        }
        return provider + "用户" + shortToken(providerUserId);
    }

    private String buildSyntheticPassword(String provider, String providerUserId) {
        return provider + ":" + providerUserId + ":campusaid";
    }

    private String shortToken(String value) {
        String encoded = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return encoded.length() > 16 ? encoded.substring(0, 16) : encoded;
    }

}
