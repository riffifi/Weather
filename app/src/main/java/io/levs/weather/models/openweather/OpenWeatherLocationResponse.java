package io.levs.weather.models.openweather;

import com.google.gson.annotations.SerializedName;

/**
 * Model class for OpenWeatherMap Geocoding API response
 */
public class OpenWeatherLocationResponse {
    @SerializedName("name")
    private String name;
    
    @SerializedName("lat")
    private double lat;
    
    @SerializedName("lon")
    private double lon;
    
    @SerializedName("country")
    private String country;
    
    @SerializedName("state")
    private String state;
    
    // Getters
    public String getName() {
        return name;
    }
    
    public double getLat() {
        return lat;
    }
    
    public double getLon() {
        return lon;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getState() {
        return state;
    }
    
    // Helper to get full location name
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(name != null ? name : "Unknown");
        
        // Add state
        if (state != null && !state.isEmpty()) {
            fullName.append(", ").append(state);
        }
        
        // Add country
        if (country != null && !country.isEmpty()) {
            fullName.append(", ").append(country);
        }
        
        return fullName.toString();
    }
}
