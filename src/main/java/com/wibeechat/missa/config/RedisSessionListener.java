package com.wibeechat.missa.config;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisSessionListener {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String SESSION_USER_PREFIX = "session:user:";
    private static final int SESSION_TIMEOUT = 30 * 60; // 30분

    public void sessionCreated(String sessionId, String userId) {
        try {
            redisTemplate.opsForValue().set(USER_SESSION_PREFIX + userId, sessionId, SESSION_TIMEOUT, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(SESSION_USER_PREFIX + sessionId, userId, SESSION_TIMEOUT, TimeUnit.SECONDS);
            log.info("Redis에 세션-사용자 매핑 저장 완료");
        } catch (Exception e) {
            log.error("Redis 저장 오류: {}", e.getMessage());
        }
    }

    public void sessionDestroyed(String sessionId) {
        try {
            String userId = (String) redisTemplate.opsForValue().get(SESSION_USER_PREFIX + sessionId);
            if (userId != null) {
                deleteSessionData(userId, sessionId);
                log.info("Redis 세션 데이터 삭제 완료");
            }
        } catch (Exception e) {
            log.error("Redis 세션 삭제 오류: {}", e.getMessage());
        }
    }

    private void deleteSessionData(String userId, String sessionId) {
        redisTemplate.delete(USER_SESSION_PREFIX + userId);
        redisTemplate.delete(SESSION_USER_PREFIX + sessionId);
    }

    public boolean isValidSession(String userId) {
        try {
            String sessionId = (String) redisTemplate.opsForValue().get(USER_SESSION_PREFIX + userId);
            return sessionId != null && Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_USER_PREFIX + sessionId));
        } catch (Exception e) {
            log.error("Redis 검증 오류: {}", e.getMessage());
            return false;
        }
    }

    public void refreshSessionTTL(String userId) {
        try {
            String sessionId = (String) redisTemplate.opsForValue().get(USER_SESSION_PREFIX + userId);
            if (sessionId != null) {
                redisTemplate.expire(USER_SESSION_PREFIX + userId, SESSION_TIMEOUT, TimeUnit.SECONDS);
                redisTemplate.expire(SESSION_USER_PREFIX + sessionId, SESSION_TIMEOUT, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("TTL 갱신 오류: {}", e.getMessage());
        }
    }
}
