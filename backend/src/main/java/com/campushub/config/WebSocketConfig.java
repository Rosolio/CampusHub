package com.campushub.config;

import com.campushub.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final OnlineWebSocketHandler onlineHandler;
    private final JwtUtil jwtUtil;

    public WebSocketConfig(OnlineWebSocketHandler onlineHandler, JwtUtil jwtUtil) {
        this.onlineHandler = onlineHandler;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(onlineHandler, "/ws/online")
                .setAllowedOrigins("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        URI uri = request.getURI();
                        String query = uri.getQuery();
                        if (query == null) return false;

                        String token = null;
                        for (String param : query.split("&")) {
                            if (param.startsWith("token=")) {
                                token = param.substring(6);
                                break;
                            }
                        }

                        if (token == null || token.isEmpty()) return false;

                        try {
                            if (jwtUtil.isTokenExpired(token)) return false;
                            Long userId = jwtUtil.getUserIdFromToken(token);
                            attributes.put("userId", userId);
                            return true;
                        } catch (Exception e) {
                            log.warn("WebSocket auth failed: {}", e.getMessage());
                            return false;
                        }
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                });
    }
}
