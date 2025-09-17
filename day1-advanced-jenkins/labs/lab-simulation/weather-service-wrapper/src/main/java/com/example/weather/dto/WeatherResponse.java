package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class WeatherResponse {
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("temperature")
    private Double temperature;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("humidity")
    private Integer humidity;
    
    @JsonProperty("pressure")
    private Double pressure;
    
    @JsonProperty("wind_speed")
    private Double windSpeed;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("status")
    private String status;
    
    // Constructors
    public WeatherResponse() {
        this.timestamp = LocalDateTime.now();
        this.status = "success";
    }
    
    public WeatherResponse(String city, Double temperature, String description, 
                          Integer humidity, Double pressure, Double windSpeed, String source) {
        this();
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.source = source;
    }
    
    // Getters and Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    
    public Double getPressure() { return pressure; }
    public void setPressure(Double pressure) { this.pressure = pressure; }
    
    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
