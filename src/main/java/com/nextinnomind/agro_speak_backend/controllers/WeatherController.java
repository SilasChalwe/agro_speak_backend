package com.nextinnomind.agro_speak_backend.controllers;

import com.nextinnomind.agro_speak_backend.entity.WeatherResponse;
import com.nextinnomind.agro_speak_backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Get current weather conditions
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(
            @RequestParam double latitude,
            @RequestParam double longitude,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        log.info("=== CURRENT WEATHER ENDPOINT ACCESSED ===");
        log.info("Method: GET | Endpoint: /api/v1/weather/current | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));
        log.info("Fetching current weather for coordinates: lat={}, lon={}", latitude, longitude);
        
        try {
            WeatherResponse response = weatherService.getCurrentWeather(latitude, longitude);
            log.info("Current weather data retrieved successfully for lat={}, lon={}", latitude, longitude);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching current weather for lat={}, lon={} from IP {}: {}", 
                     latitude, longitude, clientIp, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to fetch current weather data"));
        }
    }

    /**
     * Get daily forecast
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyForecast(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        log.info("=== DAILY FORECAST ENDPOINT ACCESSED ===");
        log.info("Method: GET | Endpoint: /api/v1/weather/daily | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));
        log.info("Fetching {}-day forecast for coordinates: lat={}, lon={}", days, latitude, longitude);
        
        try {
            WeatherResponse response = weatherService.getDailyForecast(latitude, longitude, days);
            log.info("Daily forecast data retrieved successfully for lat={}, lon={} ({} days)", 
                     latitude, longitude, days);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching daily forecast for lat={}, lon={} from IP {}: {}", 
                     latitude, longitude, clientIp, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to fetch daily forecast data"));
        }
    }

    /**
     * Get hourly forecast
     */
    @GetMapping("/hourly")
    public ResponseEntity<?> getHourlyForecast(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "24") int hours,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        log.info("=== HOURLY FORECAST ENDPOINT ACCESSED ===");
        log.info("Method: GET | Endpoint: /api/v1/weather/hourly | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));
        log.info("Fetching {}-hour forecast for coordinates: lat={}, lon={}", hours, latitude, longitude);
        
        try {
            WeatherResponse response = weatherService.getHourlyForecast(latitude, longitude, hours);
            log.info("Hourly forecast data retrieved successfully for lat={}, lon={} ({} hours)", 
                     latitude, longitude, hours);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching hourly forecast for lat={}, lon={} from IP {}: {}", 
                     latitude, longitude, clientIp, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to fetch hourly forecast data"));
        }
    }

    /**
     * Get soil data for agriculture
     */
    @GetMapping("/soil")
    public ResponseEntity<?> getSoilData(
            @RequestParam double latitude,
            @RequestParam double longitude,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        log.info("=== SOIL DATA ENDPOINT ACCESSED ===");
        log.info("Method: GET | Endpoint: /api/v1/weather/soil | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));
        log.info("Fetching soil data for coordinates: lat={}, lon={}", latitude, longitude);
        
        try {
            WeatherResponse response = weatherService.getSoilData(latitude, longitude);
            log.info("Soil data retrieved successfully for lat={}, lon={}", latitude, longitude);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching soil data for lat={}, lon={} from IP {}: {}", 
                     latitude, longitude, clientIp, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to fetch soil data"));
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

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}