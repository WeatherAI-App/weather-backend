package com.weatherai.backend.model;

import lombok.Data;

@Data
public class CurrentWeather {
    private double temp;
    private double feelsLike;
    private int humidity;
    private double uvIndex;
    private double windSpeed;
    private String windDirection;
    private int rainProbability;
    private double visibility;
    private String condition;
    private int weatherCode;
    private String sunrise;
    private String sunset;
    private String bestTimeOutside;
}