package com.nextinnomind.agro_speak_backend.controllers;

import com.nextinnomind.agro_speak_backend.entity.User;
import com.nextinnomind.agro_speak_backend.repository.UserRepository;
import com.nextinnomind.agro_speak_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ---------------- Register ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest request) {
        log.info("POST /api/v1/auth/register - Registration attempt for email: {} from IP: {}", 
                user.getEmail(), getClientIpAddress(request));
        
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("POST /api/v1/auth/register - Registration failed: Email {} already exists", user.getEmail());
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Email is already registered\"}");
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("farmer"); // default role
        User savedUser = userRepository.save(user);

        log.info("POST /api/v1/auth/register - User {} registered successfully with role: {}", 
                savedUser.getEmail(), savedUser.getRole());
        
        // Return success response with user info
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", savedUser.getId());
        userInfo.put("email", savedUser.getEmail());
        userInfo.put("fullName", savedUser.getFullName());
        userInfo.put("phone", savedUser.getPhone());
        userInfo.put("role", savedUser.getRole());
        userInfo.put("createdAt", savedUser.getCreatedAt());
        response.put("user", userInfo);
        
        return ResponseEntity.ok(response);
    }

    // ---------------- Login ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        log.info("POST /api/v1/auth/login - Login attempt for email: {} from IP: {}", 
                loginRequest.getEmail(), getClientIpAddress(request));
        
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("POST /api/v1/auth/login - Login failed: User not found for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(401)
                    .body("{\"error\": \"Invalid email or password\"}");
        }

        User user = userOpt.get();

        // Check password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("POST /api/v1/auth/login - Login failed: Invalid password for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body("{\"error\": \"Invalid email or password\"}");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        log.info("POST /api/v1/auth/login - User {} logged in successfully with role: {}", 
                user.getEmail(), user.getRole());
        
        // Return token + user info
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole());
        response.put("user", userInfo);
        
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
