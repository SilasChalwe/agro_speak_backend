package com.nextinnomind.agro_speak_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.nextinnomind.agro_speak_backend.entity.WeatherResponse;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    /**
     * Convert weather code to human-readable message
     */
    private String getWeatherMessage(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Light drizzle";
            case 61, 63, 65 -> "Rainy";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75, 77 -> "Snowy";
            case 80, 81, 82 -> "Rain showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Weather data unavailable";
        };
    }

    /**
     * Current weather only
     */
    public WeatherResponse getCurrentWeather(double latitude, double longitude) {
        log.info("Fetching current weather for coordinates: lat={}, lon={}", latitude, longitude);
        
        String url = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("current_weather", true)
                .queryParam("timezone", "auto")
                .toUriString();

        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

            if (response != null && response.getCurrentWeather() != null) {
                int code = response.getCurrentWeather().getWeathercode();
                response.getCurrentWeather().setMessage(getWeatherMessage(code));
                log.info("Current weather retrieved: {} (code: {})", getWeatherMessage(code), code);
            }

            return response;
        } catch (Exception e) {
            log.error("Error fetching current weather for lat={}, lon={}: {}", latitude, longitude, e.getMessage());
            throw new RuntimeException("Failed to fetch current weather data", e);
        }
    }

    /**
     * Daily forecast for next 'daysAhead' days (max/min temps, precipitation, weather code)
     */
    public WeatherResponse getDailyForecast(double latitude, double longitude, int daysAhead) {
        log.info("Fetching {}-day forecast for coordinates: lat={}, lon={}", daysAhead, latitude, longitude);
        
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(daysAhead);

        String url = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode")
                .queryParam("start_date", start)
                .queryParam("end_date", end)
                .queryParam("timezone", "auto")
                .toUriString();

        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

            // Apply messages for daily forecast if available
            if (response != null && response.getDaily() != null && response.getDaily().getWeathercode() != null) {
                int[] codes = response.getDaily().getWeathercode();
                String[] messages = new String[codes.length];
                for (int i = 0; i < codes.length; i++) {
                    messages[i] = getWeatherMessage(codes[i]);
                }
                response.getDaily().setMessages(messages);
                log.info("Daily forecast retrieved for {} days", codes.length);
            }

            return response;
        } catch (Exception e) {
            log.error("Error fetching daily forecast for lat={}, lon={}: {}", latitude, longitude, e.getMessage());
            throw new RuntimeException("Failed to fetch daily forecast data", e);
        }
    }

    /**
     * Hourly forecast for next 'hoursAhead' hours
     */
    public WeatherResponse getHourlyForecast(double latitude, double longitude, int hoursAhead) {
        log.info("Fetching {}-hour forecast for coordinates: lat={}, lon={}", hoursAhead, latitude, longitude);
        
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays((hoursAhead / 24) + 1); // API needs dates, not hours

        String url = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", "temperature_2m,weathercode")
                .queryParam("start_date", start)
                .queryParam("end_date", end)
                .queryParam("timezone", "auto")
                .toUriString();

        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

            // Apply messages for hourly forecast if available
            if (response != null && response.getHourly() != null && response.getHourly().getWeathercode() != null) {
                int[] codes = response.getHourly().getWeathercode();
                String[] messages = new String[codes.length];
                for (int i = 0; i < codes.length; i++) {
                    messages[i] = getWeatherMessage(codes[i]);
                }
                response.getHourly().setMessages(messages);
                log.info("Hourly forecast retrieved for {} hours", Math.min(hoursAhead, codes.length));
            }

            return response;
        } catch (Exception e) {
            log.error("Error fetching hourly forecast for lat={}, lon={}: {}", latitude, longitude, e.getMessage());
            throw new RuntimeException("Failed to fetch hourly forecast data", e);
        }
    }

    /**
     * Soil data for agriculture (0-7cm soil temp & moisture)
     */
    public WeatherResponse getSoilData(double latitude, double longitude) {
        log.info("Fetching soil data for coordinates: lat={}, lon={}", latitude, longitude);
        
        String url = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", "soil_temperature_0_to_7cm,soil_moisture_0_to_7cm")
                .queryParam("timezone", "auto")
                .toUriString();

        try {
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            log.info("Soil data retrieved successfully for lat={}, lon={}", latitude, longitude);
            return response;
        } catch (Exception e) {
            log.error("Error fetching soil data for lat={}, lon={}: {}", latitude, longitude, e.getMessage());
            throw new RuntimeException("Failed to fetch soil data", e);
        }
    }
}