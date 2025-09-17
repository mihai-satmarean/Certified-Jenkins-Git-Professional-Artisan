package com.example.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
public class WeatherData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "city_name", nullable = false)
    private String cityName;
    
    @NotNull
    @Column(name = "temperature", nullable = false)
    private Double temperature;
    
    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;
    
    @NotNull
    @Column(name = "humidity", nullable = false)
    private Integer humidity;
    
    @NotNull
    @Column(name = "pressure", nullable = false)
    private Double pressure;
    
    @NotNull
    @Column(name = "wind_speed", nullable = false)
    private Double windSpeed;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "source", nullable = false)
    private String source; // "mock" or "api"
    
    // Constructors
    public WeatherData() {
        this.timestamp = LocalDateTime.now();
    }
    
    public WeatherData(String cityName, Double temperature, String description, 
                      Integer humidity, Double pressure, Double windSpeed, String source) {
        this();
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.source = source;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    
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
}
