package io.levs.weather.models;

public class DailyForecast {
    private String day;
    private String highTemp;
    private String lowTemp;
    private int iconResource;

    public DailyForecast() {
    }

    public DailyForecast(String day, String highTemp, String lowTemp, int iconResource) {
        this.day = day;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.iconResource = iconResource;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
} 