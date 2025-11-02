package com.nextinnomind.agro_speak_backend.service;

import com.nextinnomind.agro_speak_backend.entity.User;
import com.nextinnomind.agro_speak_backend.entity.WeatherResponse;
import com.nextinnomind.agro_speak_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherAlertScheduler {

    private final UserRepository userRepository;
    private final WeatherService weatherService;
    private final SmsService smsService;

    @Value("${weather.alerts.enabled:true}")
    private boolean alertsEnabled;

    @Value("${weather.alerts.precipitation-threshold:20.0}")
    private double precipitationThreshold;

    @Value("${weather.alerts.severe-codes:95,96,99}")
    private String severeCodes;

    @Value("${weather.alerts.cron:0 0 * * * *}")
    private String cronExpression;

    // Run on cron configured in properties (default hourly)
    @Scheduled(cron = "${weather.alerts.cron:0 0 * * * *}")
    public void checkAndSendAlerts() {
        if (!alertsEnabled) {
            log.debug("Weather alerts disabled; skipping scheduler run.");
            return;
        }

        log.info("Running weather alert scheduler");

        List<Integer> severe = Arrays.stream(severeCodes.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        List<User> users = userRepository.findAll();
        for (User u : users) {
            try {
                if (u.getAlertsEnabled() == null || !u.getAlertsEnabled()) continue;
                if (u.getPhone() == null || u.getPhone().isEmpty()) continue;

                Double lat = u.getLatitude();
                Double lon = u.getLongitude();
                if (lat == null || lon == null) {
                    // skip users without a preferred location
                    log.debug("Skipping user {} - no preferred location set", u.getEmail());
                    continue;
                }

                WeatherResponse current = weatherService.getCurrentWeather(lat, lon);
                if (current == null || current.getCurrentWeather() == null) continue;

                int code = current.getCurrentWeather().getWeathercode();
                
                // Get next 24 hours forecast for precipitation check
                WeatherResponse hourlyForecast = weatherService.getHourlyForecast(lat, lon, 24);
                Double maxPrecipitation = null;
                
                if (hourlyForecast != null && hourlyForecast.getHourly() != null 
                    && hourlyForecast.getHourly().getPrecipitation() != null) {
                    // Find maximum precipitation in next 24 hours
                    @SuppressWarnings("null")
                    Double maxPrecip = Arrays.stream(hourlyForecast.getHourly().getPrecipitation())
                            .filter(p -> p != null)
                            .max(Double::compare)
                            .orElse(null);
                    maxPrecipitation = maxPrecip;
                }

                StringBuilder alertBuilder = new StringBuilder();
                boolean shouldAlert = false;

                if (severe.contains(code)) {
                    alertBuilder.append("Severe weather alert: ")
                            .append(current.getCurrentWeather().getMessage())
                            .append(" at location (")
                            .append(lat).append(",").append(lon).append(").");
                    shouldAlert = true;
                }

                if (maxPrecipitation != null && maxPrecipitation >= precipitationThreshold) {
                    if (shouldAlert) {
                        alertBuilder.append(" ");
                    }
                    alertBuilder.append("Heavy precipitation expected: ")
                            .append(String.format("%.1f", maxPrecipitation)).append("mm.");
                    shouldAlert = true;
                }

                if (shouldAlert) {
                    String body = alertBuilder.toString();
                    smsService.sendSms(u.getPhone(), body);
                }

            } catch (Exception e) {
                log.error("Failed to check/send alert for user {}: {}", u.getEmail(), e.getMessage(), e);
            }
        }

        log.info("Weather alert scheduler run complete");
    }
}
