package com.campushub.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineSessionManager {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public boolean isOnline(Long userId) {
        if (userId == null) return false;
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
}
