package com.nextinnomind.agro_speak_backend.controllers;

import com.nextinnomind.agro_speak_backend.entity.WeatherResponse;
import com.nextinnomind.agro_speak_backend.service.WeatherService;
import com.nextinnomind.agro_speak_backend.service.SmsService;
import com.nextinnomind.agro_speak_backend.repository.UserRepository;
import com.nextinnomind.agro_speak_backend.entity.User;
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
    private final SmsService smsService;
    private final UserRepository userRepository;

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
     * Send forecast via SMS to a phone number (ad-hoc)
     */
    @PostMapping("/sms/forecast")
    public ResponseEntity<?> sendForecastSms(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam String phone,
            HttpServletRequest request) {

        String clientIp = getClientIpAddress(request);
        log.info("=== SMS FORECAST REQUESTED ===");
        log.info("Method: POST | Endpoint: /api/v1/weather/sms/forecast | IP: {} | User-Agent: {}", 
                 clientIp, request.getHeader("User-Agent"));

        try {
            WeatherResponse resp = weatherService.getDailyForecast(latitude, longitude, 3);
            StringBuilder body = new StringBuilder();
            body.append("3-day forecast:\n");
            if (resp != null && resp.getDaily() != null) {
                String[] times = resp.getDaily().getTime();
                double[] tmax = resp.getDaily().getTemperatureMax();
                double[] tmin = resp.getDaily().getTemperatureMin();
                String[] messages = resp.getDaily().getMessages();
                int n = Math.min(times.length, Math.min(tmax.length, tmin.length));
                for (int i = 0; i < n; i++) {
                    body.append(times[i]).append(": ")
                        .append(messages != null && messages.length > i ? messages[i] : "")
                        .append(" - ").append(tmin[i]).append("/" ).append(tmax[i]).append(" C\n");
                }
            } else {
                body.append("Forecast not available.");
            }

            boolean sent = smsService.sendSms(phone, body.toString());
            if (sent) return ResponseEntity.ok().body(Map.of("message", "SMS sent"));
            return ResponseEntity.status(503).body(createErrorResponse("SMS sending failed or not configured"));

        } catch (Exception e) {
            log.error("Error sending SMS forecast to {} from IP {}: {}", phone, clientIp, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to send SMS forecast"));
        }
    }

    /**
     * Subscribe or unsubscribe the authenticated user's phone for proactive alerts
     */
    @PostMapping("/alerts/subscribe")
    public ResponseEntity<?> subscribeAlerts(@RequestParam Long userId,
                                             @RequestParam boolean enable,
                                             @RequestParam(required = false) Double latitude,
                                             @RequestParam(required = false) Double longitude) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.status(404).body(createErrorResponse("User not found"));

            user.setAlertsEnabled(enable);
            if (latitude != null && longitude != null) {
                user.setLatitude(latitude);
                user.setLongitude(longitude);
            }
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Subscription updated", "alertsEnabled", enable));
        } catch (Exception e) {
            log.error("Failed to update subscription: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to update subscription"));
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