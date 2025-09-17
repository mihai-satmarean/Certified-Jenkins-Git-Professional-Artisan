package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.model.WeatherData;
import com.example.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {
    
    @Autowired
    private WeatherService weatherService;
    
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam String city,
            @RequestParam(defaultValue = "false") boolean useRealApi) {
        
        try {
            WeatherResponse response = weatherService.getWeatherData(city, useRealApi);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            WeatherResponse errorResponse = new WeatherResponse();
            errorResponse.setCity(city);
            errorResponse.setStatus("error");
            errorResponse.setDescription("Error fetching weather data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/history/{city}")
    public ResponseEntity<List<WeatherData>> getWeatherHistory(@PathVariable String city) {
        try {
            List<WeatherData> history = weatherService.getWeatherHistory(city);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/recent/{city}")
    public ResponseEntity<List<WeatherData>> getRecentWeather(
            @PathVariable String city,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<WeatherData> recent = weatherService.getRecentWeatherData(city, hours);
            return ResponseEntity.ok(recent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getWeatherStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("mockRequests", weatherService.getRequestCountBySource("mock"));
            stats.put("apiRequests", weatherService.getRequestCountBySource("api"));
            stats.put("cityRequestCounts", weatherService.getCityRequestCounts());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Weather Service Wrapper");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
