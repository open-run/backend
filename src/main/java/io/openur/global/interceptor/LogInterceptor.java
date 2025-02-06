package io.openur.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/health") && response.getStatus() == 200) {
            return true;
        }
        log.info("[API REQUEST] " + requestURI);
        return true;
    }

    @Override
    public void postHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler,
        @Nullable ModelAndView modelAndView
    ) throws Exception {
        String requestURI = request.getRequestURI();
        int status = response.getStatus();
        if (requestURI.equals("/health") && status == 200) {
            return;
        }
        log.info("[API RESPONSE] " + requestURI + ": " + status);
    }
}

