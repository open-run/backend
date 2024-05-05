package io.openur.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/health") && response.getStatus() == 200) {
            return true;
        }
        log.info("[API REQUEST] " + requestURI);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        String requestURI = request.getRequestURI();
        int status = response.getStatus();
        if (requestURI.equals("/health") && status == 200) {
            return;
        }
        log.info("[API RESPONSE] " + requestURI + ": " + status);
    }
}

