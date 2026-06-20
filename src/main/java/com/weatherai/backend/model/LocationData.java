package com.weatherai.backend.model;

import lombok.Data;

@Data
public class LocationData {
    private String road;
    private String district;
    private String city;
    private String country;
    private String displayName; // full formatted address
}