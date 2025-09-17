package com.example.weather.service;

import com.example.weather.model.WeatherData;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MockWeatherService {
    
    private final Random random = new Random();
    private final Map<String, WeatherData> mockData = new HashMap<>();
    
    public MockWeatherService() {
        initializeMockData();
    }
    
    private void initializeMockData() {
        // Bucharest weather data
        mockData.put("bucharest", new WeatherData(
            "Bucharest", 22.5, "Partly cloudy", 65, 1013.25, 12.5, "mock"
        ));
        
        // London weather data
        mockData.put("london", new WeatherData(
            "London", 15.2, "Rainy", 80, 1008.75, 18.3, "mock"
        ));
        
        // Paris weather data
        mockData.put("paris", new WeatherData(
            "Paris", 18.7, "Sunny", 55, 1015.50, 8.9, "mock"
        ));
        
        // New York weather data
        mockData.put("new york", new WeatherData(
            "New York", 12.3, "Cloudy", 70, 1010.25, 15.7, "mock"
        ));
        
        // Tokyo weather data
        mockData.put("tokyo", new WeatherData(
            "Tokyo", 25.8, "Clear", 60, 1018.75, 6.2, "mock"
        ));
    }
    
    public WeatherData getMockWeatherData(String cityName) {
        String normalizedCity = cityName.toLowerCase().trim();
        
        if (mockData.containsKey(normalizedCity)) {
            WeatherData baseData = mockData.get(normalizedCity);
            return createVariedWeatherData(baseData);
        }
        
        // Generate random weather data for unknown cities
        return generateRandomWeatherData(cityName);
    }
    
    private WeatherData createVariedWeatherData(WeatherData baseData) {
        // Add some variation to make it more realistic
        double tempVariation = (random.nextDouble() - 0.5) * 4; // ±2°C
        int humidityVariation = random.nextInt(11) - 5; // ±5%
        double pressureVariation = (random.nextDouble() - 0.5) * 10; // ±5 hPa
        double windVariation = (random.nextDouble() - 0.5) * 6; // ±3 m/s
        
        return new WeatherData(
            baseData.getCityName(),
            Math.round((baseData.getTemperature() + tempVariation) * 10.0) / 10.0,
            baseData.getDescription(),
            Math.max(0, Math.min(100, baseData.getHumidity() + humidityVariation)),
            Math.round((baseData.getPressure() + pressureVariation) * 100.0) / 100.0,
            Math.max(0, Math.round((baseData.getWindSpeed() + windVariation) * 10.0) / 10.0),
            "mock"
        );
    }
    
    private WeatherData generateRandomWeatherData(String cityName) {
        String[] descriptions = {"Sunny", "Partly cloudy", "Cloudy", "Rainy", "Clear", "Overcast"};
        String description = descriptions[random.nextInt(descriptions.length)];
        
        double temperature = 5 + random.nextDouble() * 30; // 5-35°C
        int humidity = 30 + random.nextInt(50); // 30-80%
        double pressure = 1000 + random.nextDouble() * 30; // 1000-1030 hPa
        double windSpeed = random.nextDouble() * 25; // 0-25 m/s
        
        return new WeatherData(
            cityName,
            Math.round(temperature * 10.0) / 10.0,
            description,
            humidity,
            Math.round(pressure * 100.0) / 100.0,
            Math.round(windSpeed * 10.0) / 10.0,
            "mock"
        );
    }
}
