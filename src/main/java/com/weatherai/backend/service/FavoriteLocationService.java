package com.weatherai.backend.service;

import com.weatherai.backend.dto.FavoriteLocationResponse;
import com.weatherai.backend.entity.FavoriteLocation;
import com.weatherai.backend.entity.User;
import com.weatherai.backend.repository.FavoriteLocationRepository;
import com.weatherai.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class FavoriteLocationService {

    @Autowired
    private FavoriteLocationRepository favoriteLocationRepository;

    @Autowired
    private UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Get all favorites for a user
    public List<FavoriteLocationResponse> getFavorites(String email) {
        User user = getUser(email);
        return favoriteLocationRepository.findByUser(user)
                .stream()
                .map(loc -> new FavoriteLocationResponse(
                        loc.getId(),
                        loc.getCityName(),
                        loc.getCountry(),
                        loc.getLat(),
                        loc.getLon(),
                        loc.getCreatedAt()
                ))
                .toList();
    }

    // Add a favorite
    public FavoriteLocationResponse addFavorite(String email, String cityName,
                                                double lat, double lon, String country) {
        User user = getUser(email);

        if (favoriteLocationRepository.existsByUserAndCityName(user, cityName)) {
            throw new RuntimeException(cityName + " is already in your favorites");
        }

        FavoriteLocation location = new FavoriteLocation();
        location.setUser(user);
        location.setCityName(cityName);
        location.setLat(lat);
        location.setLon(lon);
        location.setCountry(country);

        FavoriteLocation saved = favoriteLocationRepository.save(location);

        return new FavoriteLocationResponse(
                saved.getId(),
                saved.getCityName(),
                saved.getCountry(),
                saved.getLat(),
                saved.getLon(),
                saved.getCreatedAt()
        );
    }

    // Remove a favorite
    public void removeFavorite(String email, String locationId) {
        User user = getUser(email);
        FavoriteLocation location = favoriteLocationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        if (!location.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        favoriteLocationRepository.delete(location);
    }
}