package com.nextinnomind.agro_speak_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for CORS settings
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests
     */
    private List<String> allowedOrigins = List.of(
        "http://localhost:3000",
        "http://localhost:4200", 
        "http://localhost:8080"
    );

    /**
     * List of allowed HTTP methods
     */
    private List<String> allowedMethods = List.of(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
    );

    /**
     * List of allowed headers
     */
    private List<String> allowedHeaders = List.of(
        "Authorization",
        "Cache-Control",
        "Content-Type",
        "Accept",
        "X-Requested-With",
        "Access-Control-Allow-Origin",
        "Access-Control-Allow-Headers",
        "Origin"
    );

    /**
     * Whether to allow credentials in CORS requests
     */
    private boolean allowCredentials = true;

    /**
     * Maximum age for CORS preflight cache (in seconds)
     */
    private long maxAge = 3600;
}