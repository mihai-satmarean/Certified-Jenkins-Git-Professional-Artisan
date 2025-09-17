package com.example.weather.service;

import com.example.weather.model.WeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenWeatherMapService {
    
    private final WebClient webClient;
    private final String apiKey;
    
    public OpenWeatherMapService(@Value("${weather.api.key:0c624616aac8eb7faa80df140cb139d7}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("http://api.openweathermap.org/data/2.5")
                .build();
    }
    
    public Mono<WeatherData> getWeatherData(String cityName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("q", cityName)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapToWeatherData)
                .onErrorReturn(createErrorWeatherData(cityName));
    }
    
    private WeatherData mapToWeatherData(Map<String, Object> response) {
        String cityName = (String) response.get("name");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> main = (Map<String, Object>) response.get("main");
        @SuppressWarnings("unchecked")
        Map<String, Object> weather = (Map<String, Object>) ((java.util.List<?>) response.get("weather")).get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        
        Double temperature = ((Number) main.get("temp")).doubleValue();
        Integer humidity = ((Number) main.get("humidity")).intValue();
        Double pressure = ((Number) main.get("pressure")).doubleValue();
        String description = (String) weather.get("description");
        Double windSpeed = wind.get("speed") != null ? 
                ((Number) wind.get("speed")).doubleValue() : 0.0;
        
        return new WeatherData(
                cityName,
                Math.round(temperature * 10.0) / 10.0,
                description,
                humidity,
                pressure,
                Math.round(windSpeed * 10.0) / 10.0,
                "api"
        );
    }
    
    private WeatherData createErrorWeatherData(String cityName) {
        return new WeatherData(
                cityName,
                0.0,
                "Error fetching data",
                0,
                0.0,
                0.0,
                "api-error"
        );
    }
}
