package io.levs.weather.models;

/**
 * Model class representing a location with weather information
 */
public class Location {
    private String id;
    private String name;
    private String condition;
    private String temperature;
    private boolean isCurrentLocation;

    public Location(String id, String name, String condition, String temperature, boolean isCurrentLocation) {
        this.id = id;
        this.name = name;
        this.condition = condition;
        this.temperature = temperature;
        this.isCurrentLocation = isCurrentLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public boolean isCurrentLocation() {
        return isCurrentLocation;
    }

    public void setCurrentLocation(boolean currentLocation) {
        isCurrentLocation = currentLocation;
    }
}
