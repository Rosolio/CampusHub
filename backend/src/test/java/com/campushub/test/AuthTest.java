package com.campushub.test;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.RegisterRequest;
import com.campushub.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setStudentId("20230001");
        request.setName("测试用户");
        request.setEmail("test@example.com");
        request.setPassword("123456");

        Map<String, Object> result = authService.register(request);
        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));
    }

    @Test
    public void testLogin() {
        LoginRequest request = new LoginRequest();
        request.setStudentId("20230001");
        request.setPassword("123456");

        Map<String, Object> result = authService.login(request);
        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));
    }

    @Test
    public void testRefreshToken() {
        // 先登录获取refreshToken
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setStudentId("20230001");
        loginRequest.setPassword("123456");

        Map<String, Object> loginResult = authService.login(loginRequest);
        String refreshToken = (String) loginResult.get("refreshToken");

        // 测试刷新令牌
        String newToken = authService.refreshToken(refreshToken);
        assertNotNull(newToken);
    }

}
