package com.weatherai.backend.controller;

import com.weatherai.backend.model.WeatherResponse;
import com.weatherai.backend.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/forecast")
    public ResponseEntity<WeatherResponse> getForecast(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) String cityName) {
        WeatherResponse response = weatherService.getWeatherWithSuggestions(lat, lon, cityName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<WeatherResponse> getWeatherByCity(
            @RequestParam String city) {
        WeatherResponse response = weatherService.getWeatherByCity(city);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<Map<String, Object>>> autocompleteCity(
            @RequestParam String city) {
        List<Map<String, Object>> results = weatherService.autocompleteCity(city);
        return ResponseEntity.ok(results);
    }
}