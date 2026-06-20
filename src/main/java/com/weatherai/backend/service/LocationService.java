package com.weatherai.backend.service;

import com.weatherai.backend.model.LocationData;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public LocationData getLocation(double lat, double lon) {
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse" +
                        "?lat=%s&lon=%s&format=json",
                lat, lon
        );

        HttpHeaders headers = new HttpHeaders();
        // Required by Nominatim - identify your app
        headers.set("User-Agent", "WeatherAI-App/1.0");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, request, Map.class
        );

        Map<String, Object> body = response.getBody();
        Map<String, Object> address = (Map<String, Object>) body.get("address");

        LocationData location = new LocationData();

        // Extract address parts safely
        location.setRoad(getOrDefault(address, "road"));
        location.setDistrict(
                address.containsKey("suburb") ? (String) address.get("suburb") :
                        address.containsKey("quarter") ? (String) address.get("quarter") :
                                getOrDefault(address, "district")
        );
        location.setCity(
                address.containsKey("city") ? (String) address.get("city") :
                        getOrDefault(address, "town")
        );
        location.setCountry(getOrDefault(address, "country"));
        location.setDisplayName((String) body.get("display_name"));

        return location;
    }

    private String getOrDefault(Map<String, Object> map, String key) {
        return map.containsKey(key) ? (String) map.get(key) : "";
    }
}