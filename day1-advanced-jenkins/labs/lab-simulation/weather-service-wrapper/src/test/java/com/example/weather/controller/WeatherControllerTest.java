package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void testGetWeatherWithMockData() throws Exception {
        WeatherResponse mockResponse = new WeatherResponse(
                "Bucharest", 22.5, "Partly cloudy", 65, 1013.25, 12.5, "mock"
        );

        when(weatherService.getWeatherData(any(), anyBoolean())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/weather")
                .param("city", "Bucharest")
                .param("useRealApi", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Bucharest"))
                .andExpect(jsonPath("$.temperature").value(22.5))
                .andExpect(jsonPath("$.description").value("Partly cloudy"))
                .andExpect(jsonPath("$.source").value("mock"))
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void testGetWeatherWithRealApi() throws Exception {
        WeatherResponse apiResponse = new WeatherResponse(
                "Bucharest", 25.3, "Clear sky", 60, 1015.50, 8.2, "api"
        );

        when(weatherService.getWeatherData(any(), anyBoolean())).thenReturn(apiResponse);

        mockMvc.perform(get("/api/weather")
                .param("city", "Bucharest")
                .param("useRealApi", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Bucharest"))
                .andExpect(jsonPath("$.source").value("api"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/weather/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Weather Service Wrapper"));
    }
}
