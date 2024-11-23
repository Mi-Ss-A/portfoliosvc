package com.wibeechat.missa.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionManager {
    // userId를 키로 사용하는 Map으로 변경
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_SESSION_PREFIX = "spring:session:sessions:";
    private static final String REDIS_EXPIRES_PREFIX = "spring:session:sessions:expires:";

    public void addSession(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            sessions.put(userId, session);
            log.info("Session added for user: {}", userId);
        } else {
            log.warn("Cannot add session: userId not found in session");
        }
    }

    public void removeSession(String userId) {
        log.info("Attempting to remove session for user: {}", userId);

        try {
            HttpSession session = sessions.remove(userId);
            if (session != null) {
                // Redis에서 세션 제거
                String sessionId = session.getId();
                String sessionKey = REDIS_SESSION_PREFIX + sessionId;
                String expiresKey = REDIS_EXPIRES_PREFIX + sessionId;

                redisTemplate.delete(sessionKey);
                redisTemplate.delete(expiresKey);

                session.invalidate();
                log.info("Session removed successfully for user: {}", userId);
            } else {
                log.warn("Session not found for user: {}", userId);
            }
        } catch (IllegalStateException e) {
            log.warn("Session already invalidated for user: {}", userId);
        } catch (Exception e) {
            log.error("Error removing session for user: {}", userId, e);
            throw e;
        }
    }

    public HttpSession getSession(String userId) {
        return sessions.get(userId);
    }

    // 특정 사용자의 세션 존재 여부 확인
    public boolean hasSession(String userId) {
        return sessions.containsKey(userId);
    }
}