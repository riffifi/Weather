package io.levs.weather.models;

public class HourlyForecast {
    private String time;
    private String temperature;
    private int iconResource;

    public HourlyForecast() {
    }

    public HourlyForecast(String time, String temperature, int iconResource) {
        this.time = time;
        this.temperature = temperature;
        this.iconResource = iconResource;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
} 