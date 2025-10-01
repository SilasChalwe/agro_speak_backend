package com.nextinnomind.agro_speak_backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WeatherResponse {

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;

    @JsonProperty("elevation")
    private Double elevation;

    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;

    @JsonProperty("daily")
    private Daily daily;

    @JsonProperty("hourly")
    private Hourly hourly;

    // ================= Current Weather =================
    @Setter
    @Getter
    public static class CurrentWeather {
        @JsonProperty("temperature")
        private double temperature;

        @JsonProperty("windspeed")
        private double windspeed;

        @JsonProperty("winddirection")
        private double winddirection;

        @JsonProperty("weathercode")
        private int weathercode;

        @JsonProperty("is_day")
        private Integer isDay;

        @JsonProperty("time")
        private String time;

        // Custom field for human-readable message
        private String message;
    }

    // ================= Daily Forecast =================
    @Setter
    @Getter
    public static class Daily {
        @JsonProperty("time")
        private String[] time;

        @JsonProperty("temperature_2m_max")
        private double[] temperatureMax;

        @JsonProperty("temperature_2m_min")
        private double[] temperatureMin;

        @JsonProperty("precipitation_sum")
        private double[] precipitationSum;

        @JsonProperty("weathercode")
        private int[] weathercode;

        // Custom field for human-readable messages
        private String[] messages;
    }

    // ================= Hourly Forecast =================
    @Setter
    @Getter
    public static class Hourly {
        @JsonProperty("time")
        private String[] time;

        @JsonProperty("temperature_2m")
        private double[] temperature;

        @JsonProperty("weathercode")
        private int[] weathercode;

        @JsonProperty("soil_temperature_0_to_7cm")
        private double[] soilTemperature;

        @JsonProperty("soil_moisture_0_to_7cm")
        private double[] soilMoisture;

        // Custom field for human-readable messages
        private String[] messages;
    }
}