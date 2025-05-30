package io.levs.weather.models.openweather;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for OpenWeatherMap Current Weather API response
 */
public class OpenWeatherCurrentResponse {
    @SerializedName("weather")
    private List<Weather> weather;
    
    @SerializedName("main")
    private Main main;
    
    @SerializedName("wind")
    private Wind wind;
    
    @SerializedName("clouds")
    private Clouds clouds;
    
    @SerializedName("rain")
    private Rain rain;
    
    @SerializedName("snow")
    private Snow snow;
    
    @SerializedName("visibility")
    private int visibility;
    
    @SerializedName("dt")
    private long dt;
    
    @SerializedName("sys")
    private Sys sys;
    
    @SerializedName("name")
    private String name;
    
    // Nested classes
    public static class Weather {
        @SerializedName("id")
        private int id;
        
        @SerializedName("main")
        private String main;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("icon")
        private String icon;
        
        // Getters
        public int getId() {
            return id;
        }
        
        public String getMain() {
            return main;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getIcon() {
            return icon;
        }
    }
    
    public static class Main {
        @SerializedName("temp")
        private double temp;
        
        @SerializedName("feels_like")
        private double feelsLike;
        
        @SerializedName("temp_min")
        private double tempMin;
        
        @SerializedName("temp_max")
        private double tempMax;
        
        @SerializedName("pressure")
        private int pressure;
        
        @SerializedName("humidity")
        private int humidity;
        
        // Getters
        public double getTemp() {
            return temp;
        }
        
        public double getFeelsLike() {
            return feelsLike;
        }
        
        public double getTempMin() {
            return tempMin;
        }
        
        public double getTempMax() {
            return tempMax;
        }
        
        public int getPressure() {
            return pressure;
        }
        
        public int getHumidity() {
            return humidity;
        }
    }
    
    public static class Wind {
        @SerializedName("speed")
        private double speed;
        
        @SerializedName("deg")
        private int deg;
        
        @SerializedName("gust")
        private double gust;
        
        // Getters
        public double getSpeed() {
            return speed;
        }
        
        public int getDeg() {
            return deg;
        }
        
        public double getGust() {
            return gust;
        }
    }
    
    public static class Clouds {
        @SerializedName("all")
        private int all;
        
        public int getAll() {
            return all;
        }
    }
    
    public static class Rain {
        @SerializedName("1h")
        private double oneHour;
        
        @SerializedName("3h")
        private double threeHour;
        
        public double getOneHour() {
            return oneHour;
        }
        
        public double getThreeHour() {
            return threeHour;
        }
    }
    
    public static class Snow {
        @SerializedName("1h")
        private double oneHour;
        
        @SerializedName("3h")
        private double threeHour;
        
        public double getOneHour() {
            return oneHour;
        }
        
        public double getThreeHour() {
            return threeHour;
        }
    }
    
    public static class Sys {
        @SerializedName("sunrise")
        private long sunrise;
        
        @SerializedName("sunset")
        private long sunset;
        
        @SerializedName("country")
        private String country;
        
        // Getters
        public long getSunrise() {
            return sunrise;
        }
        
        public long getSunset() {
            return sunset;
        }
        
        public String getCountry() {
            return country;
        }
    }
    
    // Getters for main class
    public List<Weather> getWeather() {
        return weather;
    }
    
    public Main getMain() {
        return main;
    }
    
    public Wind getWind() {
        return wind;
    }
    
    public Clouds getClouds() {
        return clouds;
    }
    
    public Rain getRain() {
        return rain;
    }
    
    public Snow getSnow() {
        return snow;
    }
    
    public int getVisibility() {
        return visibility;
    }
    
    public long getDt() {
        return dt;
    }
    
    public Sys getSys() {
        return sys;
    }
    
    public String getName() {
        return name;
    }
}
