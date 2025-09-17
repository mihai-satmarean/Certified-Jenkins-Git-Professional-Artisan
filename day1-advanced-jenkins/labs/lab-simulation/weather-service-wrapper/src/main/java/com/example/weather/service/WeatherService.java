package com.example.weather.service;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.model.WeatherData;
import com.example.weather.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WeatherService {
    
    @Autowired
    private WeatherDataRepository weatherDataRepository;
    
    @Autowired
    private MockWeatherService mockWeatherService;
    
    @Autowired
    private OpenWeatherMapService openWeatherMapService;
    
    public WeatherResponse getWeatherData(String cityName, boolean useRealApi) {
        WeatherData weatherData;
        
        if (useRealApi) {
            // Try to get from real API first
            try {
                weatherData = openWeatherMapService.getWeatherData(cityName).block();
                if (weatherData != null && !"api-error".equals(weatherData.getSource())) {
                    weatherData = weatherDataRepository.save(weatherData);
                } else {
                    // Fallback to mock data
                    weatherData = mockWeatherService.getMockWeatherData(cityName);
                    weatherData = weatherDataRepository.save(weatherData);
                }
            } catch (Exception e) {
                // Fallback to mock data on error
                weatherData = mockWeatherService.getMockWeatherData(cityName);
                weatherData = weatherDataRepository.save(weatherData);
            }
        } else {
            // Use mock data
            weatherData = mockWeatherService.getMockWeatherData(cityName);
            weatherData = weatherDataRepository.save(weatherData);
        }
        
        return convertToResponse(weatherData);
    }
    
    public List<WeatherData> getWeatherHistory(String cityName) {
        return weatherDataRepository.findByCityNameOrderByTimestampDesc(cityName);
    }
    
    public List<WeatherData> getRecentWeatherData(String cityName, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return weatherDataRepository.findByCityNameAndTimestampAfter(cityName, since);
    }
    
    public Long getRequestCountBySource(String source) {
        return weatherDataRepository.countBySource(source);
    }
    
    public List<Object[]> getCityRequestCounts() {
        return weatherDataRepository.findCityRequestCounts();
    }
    
    private WeatherResponse convertToResponse(WeatherData weatherData) {
        return new WeatherResponse(
                weatherData.getCityName(),
                weatherData.getTemperature(),
                weatherData.getDescription(),
                weatherData.getHumidity(),
                weatherData.getPressure(),
                weatherData.getWindSpeed(),
                weatherData.getSource()
        );
    }
}
