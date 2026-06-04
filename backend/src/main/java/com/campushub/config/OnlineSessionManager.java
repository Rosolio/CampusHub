package com.campushub.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OnlineSessionManager {

    private final Map<Long, AtomicInteger> sessionCounts = new ConcurrentHashMap<>();

    public void register(Long userId, Object session) {
        if (userId == null) return;
        sessionCounts.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void unregister(Long userId, Object session) {
        if (userId == null) return;
        AtomicInteger count = sessionCounts.get(userId);
        if (count != null && count.decrementAndGet() <= 0) {
            sessionCounts.remove(userId);
        }
    }

    public boolean isOnline(Long userId) {
        if (userId == null) return false;
        AtomicInteger count = sessionCounts.get(userId);
        return count != null && count.get() > 0;
    }
}
