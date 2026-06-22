package com.weatherai.backend.service;

import com.weatherai.backend.model.CurrentWeather;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    public String getSuggestion(CurrentWeather data) {
        StringBuilder suggestion = new StringBuilder();

        // UV suggestion
        if (data.getUvIndex() >= 8) {
            suggestion.append("🌞 UV is very high! Apply SPF 50+ sunscreen and wear a hat. ");
        } else if (data.getUvIndex() >= 5) {
            suggestion.append("😎 Moderate UV today. Apply sunscreen before going out. ");
        }

        // Rain suggestion
        if (data.getRainProbability() >= 60) {
            suggestion.append("🌧️ High chance of rain — bring your umbrella! ");
        } else if (data.getRainProbability() >= 30) {
            suggestion.append("🌂 Some chance of rain — consider an umbrella. ");
        }

        // Feels like temperature
        if (data.getFeelsLike() >= 38) {
            suggestion.append("🥵 Feels like " + data.getFeelsLike() + "°C — very hot! Stay hydrated and avoid going out between 11am-3pm. ");
        } else if (data.getFeelsLike() >= 32) {
            suggestion.append("☀️ Warm day ahead. Wear light clothing and drink plenty of water. ");
        } else if (data.getFeelsLike() < 20) {
            suggestion.append("🧥 Feels cool outside — bring a jacket. ");
        }

        // Humidity
        if (data.getHumidity() >= 85) {
            suggestion.append("💧 Very high humidity — it will feel much hotter than it is. ");
        }

        // Wind
        if (data.getWindSpeed() >= 40) {
            suggestion.append("💨 Strong winds today — be careful outdoors. ");
        }

        return suggestion.toString();
    }
}