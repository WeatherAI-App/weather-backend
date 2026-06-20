package com.weatherai.backend.model;

import lombok.Data;

@Data
public class AirQuality {
    private int aqi;
    private String level;
    private String advice;
}