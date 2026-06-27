package com.weatherai.backend.service;

import com.weatherai.backend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class WeatherService {

    @Autowired
    private AIService aiService;

    @Autowired
    private LocationService locationService;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherResponse getWeatherWithSuggestions(double lat, double lon) {

        // 1. Get location
        LocationData location = locationService.getLocation(lat, lon);

        // 2. Fetch weather from Open-Meteo
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=%s&longitude=%s" +
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                        "weather_code,wind_speed_10m,wind_direction_10m,visibility" +
                        "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability," +
                        "weather_code,wind_speed_10m&forecast_hours=24" +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
                        "precipitation_probability_max,uv_index_max,sunrise,sunset" +
                        "&timezone=auto&forecast_days=7",
                lat, lon
        );

        Map<String, Object> raw = restTemplate.getForObject(url, Map.class);

        String timezone = raw.get("timezone") != null ? raw.get("timezone").toString() : "UTC";

        // 3. Parse all sections
        CurrentWeather current = parseCurrentWeather(raw);
        List<HourlyForecast> hourly = parseHourlyForecast(raw);
        List<DailyForecast> daily = parseDailyForecast(raw);
        AirQuality airQuality = fetchAirQuality(lat, lon);

        // 4. Set best time outside
        current.setBestTimeOutside(getBestTimeOutside(hourly));

        // 5. AI suggestion
        String suggestion = aiService.getSuggestion(current);

        return new WeatherResponse(location, current, hourly, daily, airQuality, suggestion, lat, lon, timezone);
    }

    // ── Current Weather ──────────────────────────────────────────
    private CurrentWeather parseCurrentWeather(Map<String, Object> raw) {
        Map<String, Object> current = (Map<String, Object>) raw.get("current");
        Map<String, Object> daily = (Map<String, Object>) raw.get("daily");

        CurrentWeather cw = new CurrentWeather();
        cw.setTemp(toDouble(current.get("temperature_2m")));
        cw.setFeelsLike(toDouble(current.get("apparent_temperature")));
        cw.setHumidity(toInt(current.get("relative_humidity_2m")));
        cw.setWindSpeed(toDouble(current.get("wind_speed_10m")));
        cw.setWindDirection(getWindDirection(toInt(current.get("wind_direction_10m"))));
        cw.setVisibility(toDouble(current.get("visibility")) / 1000); // convert m to km
        cw.setWeatherCode(toInt(current.get("weather_code")));
        cw.setCondition(getCondition(cw.getWeatherCode()));

        // UV and rain from today's daily data
        List<Double> uvList = (List<Double>) daily.get("uv_index_max");
        List<Integer> rainList = (List<Integer>) daily.get("precipitation_probability_max");
        cw.setUvIndex(uvList != null && !uvList.isEmpty() ? uvList.get(0) : 0);
        cw.setRainProbability(rainList != null && !rainList.isEmpty() ? rainList.get(0) : 0);

        // Sunrise / Sunset
        List<String> sunriseList = (List<String>) daily.get("sunrise");
        List<String> sunsetList = (List<String>) daily.get("sunset");
        cw.setSunrise(formatTime(sunriseList != null ? sunriseList.get(0) : ""));
        cw.setSunset(formatTime(sunsetList != null ? sunsetList.get(0) : ""));

        return cw;
    }

    // ── Hourly Forecast ──────────────────────────────────────────
    private List<HourlyForecast> parseHourlyForecast(Map<String, Object> raw) {
        Map<String, Object> hourly = (Map<String, Object>) raw.get("hourly");
        List<HourlyForecast> result = new ArrayList<>();

        List<String> times = (List<String>) hourly.get("time");
        List<Double> temps = (List<Double>) hourly.get("temperature_2m");
        List<Integer> humidity = (List<Integer>) hourly.get("relative_humidity_2m");
        List<Integer> rain = (List<Integer>) hourly.get("precipitation_probability");
        List<Double> wind = (List<Double>) hourly.get("wind_speed_10m");
        List<Integer> codes = (List<Integer>) hourly.get("weather_code");

        // Get current hour to filter past hours
        int currentHour = java.time.LocalTime.now().getHour();

        int count = 0;
        for (int i = 0; i < times.size() && count < 24; i++) {
            String timeStr = times.get(i);
            // Extract hour from "2026-06-22T19:00"
            int hour = Integer.parseInt(timeStr.substring(11, 13));

            // Only include current hour onwards
            if (i == 0 || hour >= currentHour || count > 0) {
                if (count == 0 && hour < currentHour) continue;

                HourlyForecast h = new HourlyForecast();
                h.setTime(timeStr.substring(11, 16)); // "19:00"
                h.setTemp(toDouble(temps.get(i)));
                h.setHumidity(humidity.get(i));
                h.setRainProbability(rain.get(i));
                h.setWindSpeed(toDouble(wind.get(i)));
                h.setCondition(getCondition(codes.get(i)));
                result.add(h);
                count++;
            }
        }

        return result;
    }

    // ── Daily Forecast ───────────────────────────────────────────
    private List<DailyForecast> parseDailyForecast(Map<String, Object> raw) {
        Map<String, Object> daily = (Map<String, Object>) raw.get("daily");
        List<DailyForecast> result = new ArrayList<>();

        List<String> dates = (List<String>) daily.get("time");
        List<Double> maxTemps = (List<Double>) daily.get("temperature_2m_max");
        List<Double> minTemps = (List<Double>) daily.get("temperature_2m_min");
        List<Integer> rain = (List<Integer>) daily.get("precipitation_probability_max");
        List<Double> uv = (List<Double>) daily.get("uv_index_max");
        List<Integer> codes = (List<Integer>) daily.get("weather_code");
        List<String> sunrises = (List<String>) daily.get("sunrise");
        List<String> sunsets = (List<String>) daily.get("sunset");

        DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 0; i < dates.size(); i++) {
            DailyForecast d = new DailyForecast();
            LocalDate date = LocalDate.parse(dates.get(i), inputFmt);
            d.setDate(date.format(dateFmt));
            d.setDayName(i == 0 ? "Today" : i == 1 ? "Tomorrow" : date.format(dayFmt));
            d.setMaxTemp(maxTemps.get(i));
            d.setMinTemp(minTemps.get(i));
            d.setRainProbability(rain.get(i));
            d.setUvIndex(uv.get(i));
            d.setCondition(getCondition(codes.get(i)));
            d.setSunrise(formatTime(sunrises.get(i)));
            d.setSunset(formatTime(sunsets.get(i)));
            result.add(d);
        }

        return result;
    }

    // ── Air Quality ──────────────────────────────────────────────
    private AirQuality fetchAirQuality(double lat, double lon) {
        try {
            String url = String.format(
                    "https://air-quality-api.open-meteo.com/v1/air-quality" +
                            "?latitude=%s&longitude=%s&current=us_aqi",
                    lat, lon
            );
            Map<String, Object> raw = restTemplate.getForObject(url, Map.class);
            Map<String, Object> current = (Map<String, Object>) raw.get("current");

            int aqi = toInt(current.get("us_aqi"));
            AirQuality aq = new AirQuality();
            aq.setAqi(aqi);

            if (aqi <= 50) {
                aq.setLevel("Good");
                aq.setAdvice("Air quality is great! Perfect for outdoor activities.");
            } else if (aqi <= 100) {
                aq.setLevel("Moderate");
                aq.setAdvice("Air quality is acceptable. Sensitive groups should take care.");
            } else if (aqi <= 150) {
                aq.setLevel("Unhealthy for Sensitive Groups");
                aq.setAdvice("Children and elderly should limit prolonged outdoor activity.");
            } else if (aqi <= 200) {
                aq.setLevel("Unhealthy");
                aq.setAdvice("Everyone should reduce outdoor activity. Consider wearing a mask.");
            } else {
                aq.setLevel("Very Unhealthy");
                aq.setAdvice("Avoid outdoor activities. Stay indoors and keep windows closed.");
            }

            return aq;

        } catch (Exception e) {
            AirQuality aq = new AirQuality();
            aq.setAqi(0);
            aq.setLevel("Unavailable");
            aq.setAdvice("Air quality data not available.");
            return aq;
        }
    }

    // ── Best Time Outside ────────────────────────────────────────
    private String getBestTimeOutside(List<HourlyForecast> hourly) {
        // Get current hour
        int currentHour = LocalTime.now().getHour();

        for (HourlyForecast h : hourly) {
            // Parse hour from time string e.g. "18:00"
            int hour = Integer.parseInt(h.getTime().split(":")[0]);

            // Only suggest daytime hours (6am - 6pm) and future hours
            if (hour >= 6 && hour <= 18 && hour >= currentHour
                    && h.getRainProbability() < 30
                    && h.getTemp() < 33) {
                return "Around " + h.getTime();
            }
        }
        return "Early morning before 7am or after sunset";
    }

    // ── Weather by City ────────────────────────────────────────────
    public WeatherResponse getWeatherByCity(String city) {
        // 1. Convert city name to lat/lon using Nominatim
        String geocodeUrl = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1",
                city.replace(" ", "+")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "WeatherAI-App/1.0");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                geocodeUrl, HttpMethod.GET, request, List.class
        );

        List<Map<String, Object>> results = response.getBody();

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("City not found: " + city);
        }

        Map<String, Object> result = results.get(0);
        double lat = Double.parseDouble((String) result.get("lat"));
        double lon = Double.parseDouble((String) result.get("lon"));

        // 2. Use existing method with the coordinates
        return getWeatherWithSuggestions(lat, lon);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private String formatTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "";
        try {
            if (dateTimeStr.contains("T")) {
                // Format: 2026-06-20T05:28
                return dateTimeStr.substring(dateTimeStr.indexOf("T") + 1);
            }
            return dateTimeStr;
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    private String getCondition(int code) {
        if (code == 0) return "Clear Sky";
        else if (code <= 2) return "Partly Cloudy";
        else if (code == 3) return "Overcast";
        else if (code <= 48) return "Foggy";
        else if (code <= 67) return "Rainy";
        else if (code <= 77) return "Snowy";
        else if (code <= 82) return "Showers";
        else if (code <= 99) return "Thunderstorm";
        else return "Unknown";
    }

    private String getWindDirection(int degrees) {
        String[] dirs = {"N","NE","E","SE","S","SW","W","NW"};
        return dirs[(int) Math.round(degrees / 45.0) % 8];
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        return ((Number) val).doubleValue();
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
}