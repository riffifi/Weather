package io.levs.weather.models.openweather;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for OpenWeatherMap Forecast API response
 */
public class OpenWeatherForecastResponse {
    @SerializedName("list")
    private List<ForecastItem> list;
    
    @SerializedName("city")
    private City city;
    
    // Nested classes
    public static class ForecastItem {
        @SerializedName("dt")
        private long dt;
        
        @SerializedName("main")
        private Main main;
        
        @SerializedName("weather")
        private List<Weather> weather;
        
        @SerializedName("clouds")
        private Clouds clouds;
        
        @SerializedName("wind")
        private Wind wind;
        
        @SerializedName("visibility")
        private int visibility;
        
        @SerializedName("pop")
        private double pop; // Probability of precipitation
        
        @SerializedName("rain")
        private Rain rain;
        
        @SerializedName("snow")
        private Snow snow;
        
        @SerializedName("dt_txt")
        private String dtTxt;
        
        // Getters
        public long getDt() {
            return dt;
        }
        
        public Main getMain() {
            return main;
        }
        
        public List<Weather> getWeather() {
            return weather;
        }
        
        public Clouds getClouds() {
            return clouds;
        }
        
        public Wind getWind() {
            return wind;
        }
        
        public int getVisibility() {
            return visibility;
        }
        
        public double getPop() {
            return pop;
        }
        
        public Rain getRain() {
            return rain;
        }
        
        public Snow getSnow() {
            return snow;
        }
        
        public String getDtTxt() {
            return dtTxt;
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
    
    public static class Clouds {
        @SerializedName("all")
        private int all;
        
        public int getAll() {
            return all;
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
    
    public static class Rain {
        @SerializedName("3h")
        private double threeHour;
        
        public double getThreeHour() {
            return threeHour;
        }
    }
    
    public static class Snow {
        @SerializedName("3h")
        private double threeHour;
        
        public double getThreeHour() {
            return threeHour;
        }
    }
    
    public static class City {
        @SerializedName("id")
        private long id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("coord")
        private Coord coord;
        
        @SerializedName("country")
        private String country;
        
        @SerializedName("population")
        private long population;
        
        @SerializedName("timezone")
        private int timezone;
        
        @SerializedName("sunrise")
        private long sunrise;
        
        @SerializedName("sunset")
        private long sunset;
        
        // Getters
        public long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public Coord getCoord() {
            return coord;
        }
        
        public String getCountry() {
            return country;
        }
        
        public long getPopulation() {
            return population;
        }
        
        public int getTimezone() {
            return timezone;
        }
        
        public long getSunrise() {
            return sunrise;
        }
        
        public long getSunset() {
            return sunset;
        }
    }
    
    public static class Coord {
        @SerializedName("lat")
        private double lat;
        
        @SerializedName("lon")
        private double lon;
        
        // Getters
        public double getLat() {
            return lat;
        }
        
        public double getLon() {
            return lon;
        }
    }
    
    // Getters for main class
    public List<ForecastItem> getList() {
        return list;
    }
    
    public City getCity() {
        return city;
    }
}
