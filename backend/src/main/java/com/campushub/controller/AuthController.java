package com.campushub.controller;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.RegisterRequest;
import com.campushub.dto.ThirdPartyLoginRequest;
import com.campushub.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newToken = authService.refreshToken(refreshToken);
        Map<String, String> result = new java.util.HashMap<>();
        result.put("token", newToken);
        return result;
    }

    @PostMapping("/third-party")
    public Map<String, Object> thirdPartyLogin(@RequestBody ThirdPartyLoginRequest request) {
        return authService.thirdPartyLogin(request);
    }

}
