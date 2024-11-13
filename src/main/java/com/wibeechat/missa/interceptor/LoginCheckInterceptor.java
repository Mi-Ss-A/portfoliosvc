package com.wibeechat.missa.interceptor;

import com.wibeechat.missa.annotation.LoginRequired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

// LoginCheckInterceptor.java
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // @LoginRequired 어노테이션이 없으면 인증 체크를 하지 않음
        if (!handlerMethod.hasMethodAnnotation(LoginRequired.class)) {
            return true;
        }

        // 세션이 있는지 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            log.info("미인증 사용자 요청");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        return true;
    }
}