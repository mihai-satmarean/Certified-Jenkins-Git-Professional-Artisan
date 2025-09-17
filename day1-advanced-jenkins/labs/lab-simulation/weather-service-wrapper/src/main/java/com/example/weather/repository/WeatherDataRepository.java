package com.example.weather.repository;

import com.example.weather.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    
    Optional<WeatherData> findFirstByCityNameOrderByTimestampDesc(String cityName);
    
    List<WeatherData> findByCityNameOrderByTimestampDesc(String cityName);
    
    @Query("SELECT w FROM WeatherData w WHERE w.cityName = :cityName AND w.timestamp >= :since ORDER BY w.timestamp DESC")
    List<WeatherData> findByCityNameAndTimestampAfter(@Param("cityName") String cityName, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(w) FROM WeatherData w WHERE w.source = :source")
    Long countBySource(@Param("source") String source);
    
    @Query("SELECT w.cityName, COUNT(w) FROM WeatherData w GROUP BY w.cityName ORDER BY COUNT(w) DESC")
    List<Object[]> findCityRequestCounts();
}
