package com.campushub.service;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.RegisterRequest;
import com.campushub.dto.UserVO;
import com.campushub.entity.User;
import com.campushub.entity.UserLoginLog;
import com.campushub.entity.UserSetting;
import com.campushub.mapper.UserLoginLogMapper;
import com.campushub.mapper.UserMapper;
import com.campushub.mapper.UserSettingMapper;
import com.campushub.util.JwtUtil;
import com.campushub.util.PasswordUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_ATTEMPT_WINDOW_MINUTES = 15;

    private final UserMapper userMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final UserSettingMapper userSettingMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthService(
        UserMapper userMapper,
        UserLoginLogMapper userLoginLogMapper,
        UserSettingMapper userSettingMapper,
        PasswordUtil passwordUtil,
        JwtUtil jwtUtil,
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.userMapper = userMapper;
        this.userLoginLogMapper = userLoginLogMapper;
        this.userSettingMapper = userSettingMapper;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> login(LoginRequest request) {
        return login(request, request == null ? null : request.getStudentId());
    }

    public Map<String, Object> login(LoginRequest request, String loginIdentifier) {
        String identifier = request.getStudentId() == null ? "" : request.getStudentId().trim();
        enforceLoginRateLimit(loginIdentifier == null ? identifier : loginIdentifier.trim());
        User user = userMapper.selectByLoginIdentifier(identifier);
        if (user == null || !passwordUtil.matches(request.getPassword(), user.getPassword())) {
            recordLoginFailure(loginIdentifier == null ? identifier : loginIdentifier.trim());
            throw new RuntimeException("用户名/学号或密码错误");
        }
        ensureUserEnabled(user);
        clearLoginFailures(loginIdentifier == null ? identifier : loginIdentifier.trim());

        String token = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        recordLogin(user.getId(), "PASSWORD");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", UserVO.from(user));
        return result;
    }

    private void enforceLoginRateLimit(String loginIdentifier) {
        String key = buildLoginAttemptKey(loginIdentifier);
        Object attempts = redisTemplate.opsForValue().get(key);
        int currentAttempts = attempts instanceof Number ? ((Number) attempts).intValue() : 0;
        if (currentAttempts >= MAX_LOGIN_ATTEMPTS) {
            throw new RuntimeException("登录失败次数过多，请 15 分钟后再试");
        }
    }

    private void recordLoginFailure(String loginIdentifier) {
        String key = buildLoginAttemptKey(loginIdentifier);
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, LOGIN_ATTEMPT_WINDOW_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void clearLoginFailures(String loginIdentifier) {
        redisTemplate.delete(buildLoginAttemptKey(loginIdentifier));
    }

    private String buildLoginAttemptKey(String loginIdentifier) {
        String normalized = safeTrim(loginIdentifier).toLowerCase(Locale.ROOT);
        return "auth:login_attempts:" + (normalized.isEmpty() ? "anonymous" : normalized);
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
                // Apply rate limiting even for implicit login via register
                enforceLoginRateLimit(existingUser.getStudentId());
                clearLoginFailures(existingUser.getStudentId());

                String token = jwtUtil.generateToken(existingUser.getId());
                String refreshToken = jwtUtil.generateRefreshToken(existingUser.getId());
                recordLogin(existingUser.getId(), "PASSWORD");

                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("refreshToken", refreshToken);
                result.put("user", UserVO.from(existingUser));
                return result;
            }

            throw new RuntimeException("该账号已注册，请直接登录");
        }

        // 创建用户
        User user = new User();
        user.setStudentId(request.getStudentId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordUtil.encryptPassword(request.getPassword()));
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setVerifiedStatus("NONE");
        user.setDisabledReason(null);
        user.setScore(BigDecimal.ZERO);
        user.setPoints(0);
        user.setLastLoginAt(LocalDateTime.now());
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
        recordLogin(user.getId(), "REGISTER");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", UserVO.from(user));
        return result;
    }

    public String refreshToken(String refreshToken) {
        try {
            String tokenType = jwtUtil.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new RuntimeException("令牌类型无效");
            }
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            return jwtUtil.generateToken(userId);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("刷新令牌无效");
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void ensureUserEnabled(User user) {
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            String reason = safeTrim(user.getDisabledReason());
            throw new RuntimeException(reason.isEmpty() ? "账号已被禁用，请联系管理员" : "账号已被禁用: " + reason);
        }
    }

    private void recordLogin(Long userId, String loginType) {
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLoginAt(userId, now);

        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setLoginType(loginType);
        log.setCreatedAt(now);
        userLoginLogMapper.insert(log);
    }

}
