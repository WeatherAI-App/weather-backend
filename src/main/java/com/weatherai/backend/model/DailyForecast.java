package com.weatherai.backend.model;

import lombok.Data;

@Data
public class DailyForecast {
    private String date;
    private String dayName;
    private double maxTemp;
    private double minTemp;
    private int rainProbability;
    private double uvIndex;
    private String condition;
    private String sunrise;
    private String sunset;
}