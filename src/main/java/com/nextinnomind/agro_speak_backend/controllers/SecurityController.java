package com.nextinnomind.agro_speak_backend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle cross-site configurations and security tokens
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/security")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class SecurityController {

    /**
     * Handle CORS preflight requests
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions(HttpServletRequest request) {
        log.info("OPTIONS /api/v1/security/** - CORS preflight request from IP: {} Origin: {}", 
                getClientIpAddress(request), request.getHeader("Origin"));
        return ResponseEntity.ok().build();
    }

    /**
     * Get CSRF token (if CSRF is enabled)
     * This endpoint can be called by frontend applications to get CSRF token
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, Object>> getCsrfToken(HttpServletRequest request) {
        log.info("GET /api/v1/security/csrf-token - CSRF token request from IP: {} Origin: {}", 
                getClientIpAddress(request), request.getHeader("Origin"));
        
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        Map<String, Object> response = new HashMap<>();
        if (csrfToken != null) {
            log.debug("CSRF token provided: {}", csrfToken.getHeaderName());
            response.put("token", csrfToken.getToken());
            response.put("headerName", csrfToken.getHeaderName());
            response.put("parameterName", csrfToken.getParameterName());
        } else {
            log.debug("CSRF protection is disabled");
            response.put("message", "CSRF protection is disabled");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for CORS verification
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(HttpServletRequest request) {
        log.info("GET /api/v1/security/health - Health check request from IP: {} Origin: {}", 
                getClientIpAddress(request), request.getHeader("Origin"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "CORS configuration is working");
        response.put("timestamp", System.currentTimeMillis());
        
        log.debug("Health check response: {}", response);
        return ResponseEntity.ok(response);
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