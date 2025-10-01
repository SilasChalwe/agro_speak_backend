package com.nextinnomind.agro_speak_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to log all HTTP requests and responses
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String origin = request.getHeader("Origin");
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Request - ");
        logMessage.append(method).append(" ").append(uri);
        
        if (queryString != null && !queryString.isEmpty()) {
            logMessage.append("?").append(queryString);
        }
        
        logMessage.append(" | IP: ").append(clientIp);
        
        if (origin != null && !origin.isEmpty()) {
            logMessage.append(" | Origin: ").append(origin);
        }
        
        if (userAgent != null && !userAgent.isEmpty()) {
            logMessage.append(" | User-Agent: ").append(userAgent);
        }
        
        log.info(logMessage.toString());
        
        // Store start time for response time calculation
        request.setAttribute("startTime", System.currentTimeMillis());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Response - ");
        logMessage.append(method).append(" ").append(uri);
        logMessage.append(" | Status: ").append(status);
        logMessage.append(" | Duration: ").append(duration).append("ms");
        
        if (ex != null) {
            logMessage.append(" | Exception: ").append(ex.getMessage());
            log.error(logMessage.toString(), ex);
        } else {
            // Log as INFO for successful requests, WARN for client errors, ERROR for server errors
            if (status >= 500) {
                log.error(logMessage.toString());
            } else if (status >= 400) {
                log.warn(logMessage.toString());
            } else {
                log.info(logMessage.toString());
            }
        }
    }

    /**
     * Helper method to get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}