package com.weatherai.backend.repository;

import com.weatherai.backend.entity.FavoriteLocation;
import com.weatherai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteLocationRepository extends JpaRepository<FavoriteLocation, String> {
    List<FavoriteLocation> findByUser(User user);
    boolean existsByUserAndCityName(User user, String cityName);
}