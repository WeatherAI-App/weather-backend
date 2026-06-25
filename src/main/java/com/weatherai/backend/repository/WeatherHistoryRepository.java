package com.weatherai.backend.repository;

import com.weatherai.backend.entity.WeatherHistory;
import com.weatherai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WeatherHistoryRepository extends JpaRepository<WeatherHistory, String> {
    List<WeatherHistory> findByUserOrderBySearchedAtDesc(User user);
}