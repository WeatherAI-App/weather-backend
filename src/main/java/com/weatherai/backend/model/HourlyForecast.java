package com.weatherai.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HourlyForecast {
    private String time;
    private double temp;
    private int rainProbability;
    private int humidity;
    private double windSpeed;
    private String condition;
    @JsonProperty("isDay")
    private boolean isDay;
}