package com.weatherai.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class WeatherResponse {
    private LocationData location;
    private CurrentWeather current;
    private List<HourlyForecast> hourly;
    private List<DailyForecast> daily;
    private AirQuality airQuality;
    private String aiSuggestion;
    private double lat;
    private double lon;
}