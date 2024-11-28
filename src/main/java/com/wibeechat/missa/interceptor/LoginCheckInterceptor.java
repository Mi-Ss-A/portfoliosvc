package com.wibeechat.missa.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.wibeechat.missa.annotation.LoginRequired;
import com.wibeechat.missa.config.RedisSessionListener;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    private final RedisSessionListener redisSessionListener;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        if (!handlerMethod.hasMethodAnnotation(LoginRequired.class)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        String userId = (String) session.getAttribute("userId");
        if (!redisSessionListener.isValidSession(userId)) {
            session.invalidate();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "세션이 만료되었습니다.");
            return false;
        }

        redisSessionListener.refreshSessionTTL(userId);
        return true;
    }
}
