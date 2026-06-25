package com.weatherai.backend.controller;

import com.weatherai.backend.service.FavoriteLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.weatherai.backend.dto.FavoriteLocationResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class FavoriteLocationController {

    @Autowired
    private FavoriteLocationService favoriteLocationService;

    // GET all favorites
    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteLocationResponse>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FavoriteLocationResponse> favorites =
                favoriteLocationService.getFavorites(userDetails.getUsername());
        return ResponseEntity.ok(favorites);
    }

    // POST add favorite
    @PostMapping("/favorites")
    public ResponseEntity<FavoriteLocationResponse> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        String cityName = (String) body.get("cityName");
        double lat = ((Number) body.get("lat")).doubleValue();
        double lon = ((Number) body.get("lon")).doubleValue();
        String country = (String) body.get("country");

        FavoriteLocationResponse saved = favoriteLocationService.addFavorite(
                userDetails.getUsername(), cityName, lat, lon, country
        );
        return ResponseEntity.ok(saved);
    }

    // DELETE remove favorite
    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        favoriteLocationService.removeFavorite(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Removed from favorites"));
    }
}