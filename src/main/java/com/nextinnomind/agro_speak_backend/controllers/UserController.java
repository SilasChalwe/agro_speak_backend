package com.nextinnomind.agro_speak_backend.controllers;

import com.nextinnomind.agro_speak_backend.entity.User;
import com.nextinnomind.agro_speak_backend.repository.UserRepository;
import com.nextinnomind.agro_speak_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Directory to store uploaded avatars
    private static final String UPLOAD_DIR = "uploads/avatars/";

    /**
     * Get user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        log.info("=== PROFILE ENDPOINT ACCESSED ===");
        log.info("Method: GET | Endpoint: /api/v1/user/profile | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String email = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // remove "Bearer "
                email = jwtUtil.extractUsername(token); // subject = email
            }
            
            log.info("Fetching profile for authenticated user: {}", email);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found in database: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found"));
            }

            User user = userOpt.get();
            Map<String, Object> profile = createUserProfileResponse(user);
            
            log.info("Profile successfully retrieved for user: {} (ID: {})", user.getEmail(), user.getId());
            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            log.error("Error retrieving user profile from IP {}: {}", clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve user profile"));
        }
    }

    /**
     * Update user profile - Fixed to handle both /profile and /profile/update endpoints
     * Also supports both "name" and "fullName" fields for frontend compatibility
     */
    @PutMapping({"/profile", "/profile/update"})
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> updateData, 
                                               HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        log.info("=== UPDATE PROFILE ENDPOINT ACCESSED ===");
        log.info("Method: PUT | Endpoint: {} | IP: {} | User-Agent: {}", 
                 request.getRequestURI(), clientIp, request.getHeader("User-Agent"));

        try {
            // Use the same JWT extraction method as getUserProfile for consistency
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String email = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // remove "Bearer "
                email = jwtUtil.extractUsername(token); // subject = email
            }

            if (email == null) {
                log.warn("No valid JWT token found for profile update from IP: {}", clientIp);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication failed - please login again"));
            }
            
            log.info("Updating profile for authenticated user: {}", email);
            log.debug("Update data received: {}", updateData);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found in database: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found"));
            }

            User user = userOpt.get();
            boolean hasChanges = false;

            // Update fullName field - support both "name" and "fullName" for frontend compatibility
            String newFullName = null;
            if (updateData.containsKey("fullName") && updateData.get("fullName") != null) {
                newFullName = updateData.get("fullName").toString().trim();
            } else if (updateData.containsKey("name") && updateData.get("name") != null) {
                // Frontend sends "name" but we treat it as "fullName"
                newFullName = updateData.get("name").toString().trim();
                log.info("Frontend sent 'name' field, treating as 'fullName' for user: {}", email);
            }

            if (newFullName != null && !newFullName.equals(user.getFullName())) {
                log.info("Updating fullName from '{}' to '{}' for user: {}", 
                         user.getFullName(), newFullName, user.getEmail());
                user.setFullName(newFullName);
                hasChanges = true;
            }

            // Update phone field
            if (updateData.containsKey("phone") && updateData.get("phone") != null) {
                String newPhone = updateData.get("phone").toString().trim();
                if (!newPhone.equals(user.getPhone())) {
                    log.info("Updating phone from '{}' to '{}' for user: {}", 
                             user.getPhone(), newPhone, user.getEmail());
                    user.setPhone(newPhone);
                    hasChanges = true;
                }
            }

            // Handle password update separately
            if (updateData.containsKey("password") && updateData.get("password") != null) {
                String newPassword = updateData.get("password").toString();
                if (newPassword.length() >= 6) { // Basic validation
                    String encodedPassword = passwordEncoder.encode(newPassword);
                    user.setPassword(encodedPassword);
                    log.info("Password updated for user: {}", user.getEmail());
                    hasChanges = true;
                } else {
                    log.warn("Invalid password length for user: {}", user.getEmail());
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Password must be at least 6 characters long"));
                }
            }

            if (!hasChanges) {
                log.info("No changes detected for user profile update: {}", user.getEmail());
                return ResponseEntity.ok(createSuccessResponse("No changes to update", createUserProfileResponse(user)));
            }

            // Save updated user
            @SuppressWarnings("null")
            User savedUser = userRepository.save(user);
            log.info("Profile successfully updated for user: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

            return ResponseEntity.ok(createSuccessResponse("Profile updated successfully", createUserProfileResponse(savedUser)));

        } catch (Exception e) {
            log.error("Error updating user profile from IP {}: {}", clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update user profile"));
        }
    }

    /**
     * Upload user avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile file, 
                                          HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        log.info("=== UPLOAD AVATAR ENDPOINT ACCESSED ===");
        log.info("Method: POST | Endpoint: /api/v1/user/avatar | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));

        try {
            // Use the same JWT extraction method for consistency
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String email = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // remove "Bearer "
                email = jwtUtil.extractUsername(token); // subject = email
            }

            if (email == null) {
                log.warn("No valid JWT token found for avatar upload from IP: {}", clientIp);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication failed - please login again"));
            }
            
            log.info("Uploading avatar for authenticated user: {}", email);
            log.info("File details - Name: {}, Size: {} bytes, Content-Type: {}", 
                     file.getOriginalFilename(), file.getSize(), file.getContentType());

            // Validate file
            if (file.isEmpty()) {
                log.warn("Empty file uploaded by user: {}", email);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Please select a file to upload"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid file type uploaded by user {}: {}", email, contentType);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only image files are allowed"));
            }

            // Validate file size (max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                log.warn("File too large uploaded by user {}: {} bytes", email, file.getSize());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File size must be less than 5MB"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found in database: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found"));
            }

            User user = userOpt.get();

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "avatar_" + user.getId() + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user's avatar path in database
            String avatarUrl = "/uploads/avatars/" + uniqueFilename;
            user.setAvatarUrl(avatarUrl);
            User savedUser = userRepository.save(user);

            log.info("Avatar successfully uploaded for user: {} (ID: {}) - File: {}", 
                     savedUser.getEmail(), savedUser.getId(), uniqueFilename);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);
            response.put("user", createUserProfileResponse(savedUser));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("File I/O error during avatar upload from IP {}: {}", clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to save uploaded file"));
        } catch (Exception e) {
            log.error("Error uploading avatar from IP {}: {}", clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload avatar"));
        }
    }

    // Helper methods

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private Map<String, Object> createUserProfileResponse(User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("updatedAt", user.getUpdatedAt());
        // Don't include password in response
        return profile;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}