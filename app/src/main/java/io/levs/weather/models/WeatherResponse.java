package io.levs.weather.models;

import java.util.List;

/**
 * Model class for storing weather data for a location
 */
public class WeatherResponse {
    private String locationKey;
    private String locationName;
    private String currentTemperature;
    private String currentCondition;
    private String highTemperature;
    private String lowTemperature;
    private String feelsLike;
    private String humidity;
    private String wind;
    private String precipitation;
    private String pressure;
    private String visibility;
    private String sunrise;
    private String sunset;
    private List<HourlyForecast> hourlyForecasts;
    private List<DailyForecast> dailyForecasts;
    
    public WeatherResponse() {
    }
    
    // getters/setters
    public String getLocationKey() {
        return locationKey;
    }
    
    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public String getCurrentTemperature() {
        return currentTemperature;
    }
    
    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }
    
    public String getCurrentCondition() {
        return currentCondition;
    }
    
    public void setCurrentCondition(String currentCondition) {
        this.currentCondition = currentCondition;
    }
    
    public String getHighTemperature() {
        return highTemperature;
    }
    
    public void setHighTemperature(String highTemperature) {
        this.highTemperature = highTemperature;
    }
    
    public String getLowTemperature() {
        return lowTemperature;
    }
    
    public void setLowTemperature(String lowTemperature) {
        this.lowTemperature = lowTemperature;
    }
    
    public String getFeelsLike() {
        return feelsLike;
    }
    
    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }
    
    public String getHumidity() {
        return humidity;
    }
    
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
    
    public String getWind() {
        return wind;
    }
    
    public void setWind(String wind) {
        this.wind = wind;
    }
    
    public String getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation;
    }
    
    public String getPressure() {
        return pressure;
    }
    
    public void setPressure(String pressure) {
        this.pressure = pressure;
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    
    public String getSunrise() {
        return sunrise;
    }
    
    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }
    
    public String getSunset() {
        return sunset;
    }
    
    public void setSunset(String sunset) {
        this.sunset = sunset;
    }
    
    public List<HourlyForecast> getHourlyForecasts() {
        return hourlyForecasts;
    }
    
    public void setHourlyForecasts(List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;
    }
    
    public List<DailyForecast> getDailyForecasts() {
        return dailyForecasts;
    }
    
    public void setDailyForecasts(List<DailyForecast> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }
}