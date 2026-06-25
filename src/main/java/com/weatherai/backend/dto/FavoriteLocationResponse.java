package com.weatherai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FavoriteLocationResponse {
    private String id;
    private String cityName;
    private String country;
    private double lat;
    private double lon;
    private LocalDateTime createdAt;
}