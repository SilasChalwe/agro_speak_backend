package com.nextinnomind.agro_speak_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC Configuration for interceptors, resource handlers, and other web-related settings
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private static final String UPLOAD_DIR = "uploads/";

    @Override
    @SuppressWarnings("null")
    public void addInterceptors(@org.springframework.lang.NonNull InterceptorRegistry registry) {
        // Add request logging interceptor for all endpoints
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/actuator/**",    // Exclude health check endpoints to reduce noise
                    "/favicon.ico",    // Exclude favicon requests
                    "/error",          // Exclude error pages
                    "/uploads/**"      // Exclude static file requests to reduce log noise
                );
    }

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Create uploads directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }
            
            Path avatarsPath = uploadPath.resolve("avatars");
            if (!Files.exists(avatarsPath)) {
                Files.createDirectories(avatarsPath);
                System.out.println("Created avatars directory: " + avatarsPath.toAbsolutePath());
            }
        } catch (Exception e) {
            // Log error but don't fail startup
            System.err.println("Could not create upload directories: " + e.getMessage());
        }

        // Serve uploaded files statically
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_DIR)
                .setCachePeriod(3600); // Cache for 1 hour
    }
}