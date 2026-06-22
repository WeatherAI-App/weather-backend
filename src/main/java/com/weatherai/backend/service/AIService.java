package com.weatherai.backend.service;

import com.weatherai.backend.model.CurrentWeather;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    public String getSuggestion(CurrentWeather data) {
        StringBuilder suggestion = new StringBuilder();

        // ── Condition-based opening ──────────────────────────
        switch (data.getCondition()) {
            case "Thunderstorm" ->
                    suggestion.append("⛈️ Thunderstorm alert! Stay indoors if possible and avoid open areas. ");
            case "Rainy", "Showers" ->
                    suggestion.append("🌧️ It's raining today — definitely bring an umbrella! ");
            case "Clear Sky" ->
                    suggestion.append("☀️ Beautiful clear sky today — great day to be outside! ");
            case "Partly Cloudy" ->
                    suggestion.append("⛅ Nice partly cloudy day — comfortable for outdoor activities. ");
            case "Overcast" ->
                    suggestion.append("☁️ Overcast skies today — no need for sunglasses. ");
            case "Foggy" ->
                    suggestion.append("🌫️ Foggy conditions — drive carefully and allow extra travel time. ");
            case "Snowy" ->
                    suggestion.append("❄️ Snowy day — wear warm layers and watch out for slippery roads. ");
        }

        // ── Heat & Feels Like ────────────────────────────────
        if (data.getFeelsLike() >= 40) {
            suggestion.append("🥵 Feels like ")
                    .append(Math.round(data.getFeelsLike()))
                    .append("°C — dangerously hot! Stay indoors between 11am-3pm, drink water every 30 mins. ");
        } else if (data.getFeelsLike() >= 35) {
            suggestion.append("🌡️ Very warm at ")
                    .append(Math.round(data.getFeelsLike()))
                    .append("°C — wear light breathable clothing and stay hydrated. ");
        } else if (data.getFeelsLike() <= 15) {
            suggestion.append("🧥 Feels cold at ")
                    .append(Math.round(data.getFeelsLike()))
                    .append("°C — layer up before heading out. ");
        }

        // ── UV Index ─────────────────────────────────────────
        if (data.getUvIndex() >= 8) {
            suggestion.append("🌞 UV is very high (")
                    .append(data.getUvIndex())
                    .append(") — apply SPF 50+ sunscreen, wear a hat and sunglasses. ");
        } else if (data.getUvIndex() >= 5) {
            suggestion.append("😎 Moderate UV (")
                    .append(data.getUvIndex())
                    .append(") — sunscreen recommended if outdoors for long. ");
        }

        // ── Rain Probability ─────────────────────────────────
        if (data.getRainProbability() >= 70) {
            suggestion.append("☔ High chance of rain (")
                    .append(data.getRainProbability())
                    .append("%) — don't leave home without an umbrella! ");
        } else if (data.getRainProbability() >= 40) {
            suggestion.append("🌂 Some chance of rain (")
                    .append(data.getRainProbability())
                    .append("%) — keep an umbrella handy just in case. ");
        }

        // ── Humidity ─────────────────────────────────────────
        if (data.getHumidity() >= 85) {
            suggestion.append("💧 Very high humidity (")
                    .append(data.getHumidity())
                    .append("%) — it will feel much hotter than it looks. Stay cool! ");
        } else if (data.getHumidity() <= 30) {
            suggestion.append("🏜️ Air is very dry (")
                    .append(data.getHumidity())
                    .append("% humidity) — drink extra water and moisturize your skin. ");
        }

        // ── Wind ─────────────────────────────────────────────
        if (data.getWindSpeed() >= 50) {
            suggestion.append("💨 Strong winds at ")
                    .append(Math.round(data.getWindSpeed()))
                    .append(" km/h — secure loose objects and be careful outdoors. ");
        } else if (data.getWindSpeed() >= 30) {
            suggestion.append("🍃 Windy at ")
                    .append(Math.round(data.getWindSpeed()))
                    .append(" km/h — hold onto your hat! ");
        }

        // ── Visibility ───────────────────────────────────────
        if (data.getVisibility() <= 1) {
            suggestion.append("🌫️ Very low visibility (")
                    .append(data.getVisibility())
                    .append(" km) — drive slowly and use fog lights. ");
        }

        // ── Perfect weather ──────────────────────────────────
        if (data.getFeelsLike() >= 22 && data.getFeelsLike() <= 28
                && data.getRainProbability() < 20
                && data.getUvIndex() < 6
                && data.getHumidity() < 70) {
            suggestion.append("🌈 Perfect weather today — ideal for outdoor activities, exercise or a picnic! ");
        }

        // ── Fallback ─────────────────────────────────────────
        if (suggestion.isEmpty()) {
            suggestion.append("🌤️ Weather looks fairly normal today. Dress comfortably and enjoy your day!");
        }

        return suggestion.toString().trim();
    }
}