package io.levs.weather.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import io.levs.weather.R;
import io.levs.weather.models.DailyForecast;
import io.levs.weather.models.HourlyForecast;
import io.levs.weather.models.Location;
import io.levs.weather.models.openweather.OpenWeatherCurrentResponse;
import io.levs.weather.models.openweather.OpenWeatherForecastResponse;
import io.levs.weather.models.openweather.OpenWeatherLocationResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for handling weather data operations
 */
public class OpenWeatherRepository {
    private static final String TAG = "OpenWeatherRepository";
    private static final String PREF_NAME = "weather_prefs";
    private static final String KEY_SAVED_LOCATIONS = "saved_locations";
    private static final String KEY_CURRENT_LOCATION = "current_location";
    private static final String PREFS_APP_NAME = "WeatherAppPreferences";
    
    private static OpenWeatherRepository instance;
    private final OpenWeatherService openWeatherService;
    private final Context context;
    
    private OpenWeatherRepository(Context context) {
        this.context = context.getApplicationContext();
        openWeatherService = RetrofitClient.getOpenWeatherService();
    }
    
    public static synchronized OpenWeatherRepository getInstance(Context context) {
        if (instance == null) {
            instance = new OpenWeatherRepository(context);
        }
        return instance;
    }
    
    /**
     * Get the OpenWeatherService instance
     * @return OpenWeatherService instance
     */
    public OpenWeatherService getOpenWeatherService() {
        return openWeatherService;
    }
    

    public interface LocationSearchCallback {
        void onSearchResults(List<Location> locations);
        void onError(String message);
    }
    

    public interface CurrentConditionsCallback {
        void onCurrentConditions(String temperature, String condition, String highTemp, String lowTemp,
                                String feelsLike, String humidity, String wind, String precipitation,
                                String pressure, String visibility, String sunrise, String sunset);
        void onError(String message);
    }
    

    public interface HourlyForecastCallback {
        void onHourlyForecast(List<HourlyForecast> hourlyForecasts);
        void onError(String message);
    }
    

    public interface DailyForecastCallback {
        void onDailyForecast(List<DailyForecast> dailyForecasts);
        void onError(String message);
    }
    
    public void searchLocationsByCity(String query, final LocationSearchCallback callback) {
        Log.d(TAG, "Searching for locations with query: " + query);
        
        final List<Location> savedLocations = loadSavedLocations();
        
        openWeatherService.searchLocationsByCity(query, 5, OpenWeatherService.API_KEY)
                .enqueue(new Callback<List<OpenWeatherLocationResponse>>() {
                    @Override
                    public void onResponse(Call<List<OpenWeatherLocationResponse>> call, 
                                          Response<List<OpenWeatherLocationResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Location> locations = new ArrayList<>();
                            Log.d(TAG, "Search successful, found " + response.body().size() + " locations");
                            
                            final int[] processedCount = {0};
                            final int totalCount = response.body().size();
                            
                            if (totalCount == 0) {
                                callback.onSearchResults(locations);
                                return;
                            }
                            
                            for (OpenWeatherLocationResponse location : response.body()) {
                                String locationId = location.getLat() + "," + location.getLon();
                                String name = location.getFullName();
                                Log.d(TAG, "Found location: " + name + " with ID: " + locationId);
                                
                                boolean isAlreadySaved = false;
                                Location savedLocation = null;
                                
                                for (Location saved : savedLocations) {
                                    if (saved.getId().equals(locationId)) {
                                        isAlreadySaved = true;
                                        savedLocation = saved;
                                        break;
                                    }
                                }
                                
                                if (isAlreadySaved && savedLocation != null) {
                                    locations.add(savedLocation);
                                    processedCount[0]++;
                                    
                                    if (processedCount[0] == totalCount) {
                                        callback.onSearchResults(locations);
                                    }
                                } else {
                                    final Location newLocation = new Location(locationId, name, "Unknown", "--°", false);
                                    locations.add(newLocation);
                                    
                                    getCurrentConditions(locationId, new CurrentConditionsCallback() {
                                        @Override
                                        public void onCurrentConditions(String temperature, String condition, String highTemp, String lowTemp,
                                                                      String feelsLike, String humidity, String wind, String precipitation,
                                                                      String pressure, String visibility, String sunrise, String sunset) {
                                            newLocation.setCondition(condition);
                                            newLocation.setTemperature(temperature);
                                            processedCount[0]++;
                                            
                                            if (processedCount[0] == totalCount) {
                                                callback.onSearchResults(locations);
                                            }
                                        }
                                        
                                        @Override
                                        public void onError(String message) {
                                            processedCount[0]++;
                                            
                                            if (processedCount[0] == totalCount) {
                                                callback.onSearchResults(locations);
                                            }
                                        }
                                    });
                                }
                            }
                        } else {
                            String errorMsg = "Failed to search locations: " + 
                                            (response.errorBody() != null ? response.errorBody().toString() : "Unknown error");
                            Log.e(TAG, errorMsg);
                            callback.onError(errorMsg);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<OpenWeatherLocationResponse>> call, Throwable t) {
                        String errorMsg = "Network error: " + t.getMessage();
                        Log.e(TAG, errorMsg, t);
                        callback.onError(errorMsg);
                    }
                });
    }
    
    /**
     * Get current weather conditions for a location
     * @param locationKey The location key (lat,lon format)
     * @param callback Callback to handle the response
     */
    public void getCurrentConditions(String locationKey, final CurrentConditionsCallback callback) {
        String[] latLon = locationKey.split(",");
        if (latLon.length != 2) {
            callback.onError("Invalid location key format");
            return;
        }
        
        try {
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            
            openWeatherService.getCurrentWeather(lat, lon, "metric", OpenWeatherService.API_KEY)
                    .enqueue(new Callback<OpenWeatherCurrentResponse>() {
                        @Override
                        public void onResponse(Call<OpenWeatherCurrentResponse> call, 
                                              Response<OpenWeatherCurrentResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                OpenWeatherCurrentResponse data = response.body();
                                
                                String condition = "Unknown";
                                if (data.getWeather() != null && !data.getWeather().isEmpty()) {
                                    condition = capitalizeFirstLetter(data.getWeather().get(0).getDescription());
                                    Log.d(TAG, "Weather condition: " + condition + ", icon: " + data.getWeather().get(0).getIcon());
                                }
                                
                                String temperature = formatTemperature(data.getMain().getTemp());
                                String feelsLike = formatTemperature(data.getMain().getFeelsLike());
                                String highTemp = formatTemperature(data.getMain().getTempMax());
                                String lowTemp = formatTemperature(data.getMain().getTempMin());
                                String humidity = data.getMain().getHumidity() + "%";
                                String wind = formatWindSpeed(data.getWind().getSpeed());
                                
                                String precipitation = "0 mm";
                                if (data.getRain() != null && data.getRain().getOneHour() > 0) {
                                    precipitation = data.getRain().getOneHour() + " mm";
                                } else if (data.getSnow() != null && data.getSnow().getOneHour() > 0) {
                                    precipitation = data.getSnow().getOneHour() + " mm";
                                }
                                
                                String pressure = data.getMain().getPressure() + " hPa";
                                String visibility = (data.getVisibility() / 1000) + " km";
                                
                                String sunrise = formatTime(data.getSys().getSunrise() * 1000);
                                String sunset = formatTime(data.getSys().getSunset() * 1000);
                                
                                callback.onCurrentConditions(
                                        temperature, condition, highTemp, lowTemp,
                                        feelsLike, humidity, wind, precipitation,
                                        pressure, visibility, sunrise, sunset);
                            } else {
                                callback.onError("Failed to get current conditions: " + 
                                                (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<OpenWeatherCurrentResponse> call, Throwable t) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    });
        } catch (NumberFormatException e) {
            callback.onError("Invalid location coordinates");
        }
    }
    
    /**
     * Get hourly forecast for a location
     * @param locationKey The location key (lat,lon format)
     * @param callback Callback to handle the response
     */
    public void getHourlyForecast(String locationKey, final HourlyForecastCallback callback) {
        // Parse lat and lon from locationKey
        String[] latLon = locationKey.split(",");
        if (latLon.length != 2) {
            callback.onError("Invalid location key format");
            return;
        }
        
        try {
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            
            // Call OpenWeatherMap API to get forecast
            openWeatherService.getForecast(lat, lon, "metric", OpenWeatherService.API_KEY)
                    .enqueue(new Callback<OpenWeatherForecastResponse>() {
                        @Override
                        public void onResponse(Call<OpenWeatherForecastResponse> call, 
                                              Response<OpenWeatherForecastResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<HourlyForecast> hourlyForecasts = new ArrayList<>();
                                
                                // Get the first 24 hours (8 items, as each is 3 hours)
                                List<OpenWeatherForecastResponse.ForecastItem> forecastItems = response.body().getList();
                                int count = Math.min(forecastItems.size(), 8);
                                
                                for (int i = 0; i < count; i++) {
                                    OpenWeatherForecastResponse.ForecastItem item = forecastItems.get(i);
                                    
                                    // Get time
                                    String time = formatHour(item.getDt() * 1000);
                                    
                                    // Get temperature
                                    String temperature = formatTemperature(item.getMain().getTemp());
                                    
                                    // Get weather condition and icon
                                    String condition = item.getWeather() != null && !item.getWeather().isEmpty() ?
                                            item.getWeather().get(0).getMain() : "Unknown";
                                    int iconResId = getWeatherIconResource(item.getWeather().get(0).getIcon());
                                    
                                    // Create hourly forecast object
                                    HourlyForecast forecast = new HourlyForecast(time, temperature, iconResId);
                                    hourlyForecasts.add(forecast);
                                }
                                
                                callback.onHourlyForecast(hourlyForecasts);
                            } else {
                                callback.onError("Failed to get hourly forecast: " + 
                                                (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<OpenWeatherForecastResponse> call, Throwable t) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    });
        } catch (NumberFormatException e) {
            callback.onError("Invalid location coordinates");
        }
    }
    
    /**
     * Get daily forecast for a location
     * @param locationKey The location key (lat,lon format)
     * @param days Number of days to get forecast for (max 5)
     * @param callback Callback to handle the response
     */
    public void getDailyForecast(String locationKey, int days, final DailyForecastCallback callback) {
        // Parse lat and lon from locationKey
        String[] latLon = locationKey.split(",");
        if (latLon.length != 2) {
            callback.onError("Invalid location key format");
            return;
        }
        
        try {
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            
            // Call OpenWeatherMap API to get forecast
            openWeatherService.getForecast(lat, lon, "metric", OpenWeatherService.API_KEY)
                    .enqueue(new Callback<OpenWeatherForecastResponse>() {
                        @Override
                        public void onResponse(Call<OpenWeatherForecastResponse> call, 
                                              Response<OpenWeatherForecastResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<DailyForecast> dailyForecasts = new ArrayList<>();
                                
                                // Process the 5-day forecast
                                // OpenWeatherMap free API provides 5-day forecast with 3-hour intervals
                                // We need to group by day and calculate min/max temperatures
                                
                                List<OpenWeatherForecastResponse.ForecastItem> forecastItems = response.body().getList();
                                
                                // Group forecasts by day
                                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                dayFormat.setTimeZone(TimeZone.getDefault());
                                
                                // Map to store daily data (date -> [min, max, condition, icon])
                                java.util.Map<String, Object[]> dailyData = new java.util.HashMap<>();
                                
                                for (OpenWeatherForecastResponse.ForecastItem item : forecastItems) {
                                    // Get date string for grouping
                                    String dateStr = dayFormat.format(new Date(item.getDt() * 1000));
                                    
                                    // Get temperature and condition
                                    double temp = item.getMain().getTemp();
                                    String condition = item.getWeather() != null && !item.getWeather().isEmpty() ?
                                            item.getWeather().get(0).getMain() : "Unknown";
                                    String icon = item.getWeather() != null && !item.getWeather().isEmpty() ?
                                            item.getWeather().get(0).getIcon() : "01d";
                                    
                                    // Update daily data
                                    if (!dailyData.containsKey(dateStr)) {
                                        // Initialize with [min, max, condition, icon]
                                        dailyData.put(dateStr, new Object[]{temp, temp, condition, icon});
                                    } else {
                                        // Update min/max temperatures
                                        Object[] data = dailyData.get(dateStr);
                                        double min = (double) data[0];
                                        double max = (double) data[1];
                                        
                                        if (temp < min) {
                                            data[0] = temp;
                                        }
                                        if (temp > max) {
                                            data[1] = temp;
                                        }
                                        
                                        // Keep noon condition if possible (around 12:00)
                                        String timeStr = item.getDtTxt().substring(11, 16);
                                        if (timeStr.startsWith("12:")) {
                                            data[2] = condition;
                                            data[3] = icon;
                                        }
                                    }
                                }
                                
                                // Convert grouped data to daily forecasts
                                SimpleDateFormat displayFormat = new SimpleDateFormat("EEE", Locale.US);
                                displayFormat.setTimeZone(TimeZone.getDefault());
                                
                                // Sort dates
                                List<String> sortedDates = new ArrayList<>(dailyData.keySet());
                                java.util.Collections.sort(sortedDates);
                                
                                // Limit to requested number of days
                                int count = Math.min(sortedDates.size(), days);
                                
                                for (int i = 0; i < count; i++) {
                                    String dateStr = sortedDates.get(i);
                                    Object[] data = dailyData.get(dateStr);
                                    
                                    // Parse date
                                    Date date;
                                    try {
                                        date = dayFormat.parse(dateStr);
                                    } catch (Exception e) {
                                        continue;
                                    }
                                    
                                    // Format day name
                                    String day = displayFormat.format(date);
                                    if (i == 0) {
                                        day = "Today";
                                    }
                                    
                                    // Format temperatures
                                    String highTemp = formatTemperature((double) data[1]);
                                    String lowTemp = formatTemperature((double) data[0]);
                                    
                                    // Get condition and icon
                                    String condition = (String) data[2];
                                    int iconResId = getWeatherIconResource((String) data[3]);
                                    
                                    // Create daily forecast object
                                    dailyForecasts.add(new DailyForecast(day, highTemp, lowTemp, iconResId));
                                }
                                
                                callback.onDailyForecast(dailyForecasts);
                            } else {
                                callback.onError("Failed to get daily forecast: " + 
                                                (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<OpenWeatherForecastResponse> call, Throwable t) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    });
        } catch (NumberFormatException e) {
            callback.onError("Invalid location coordinates");
        }
    }
    
    /**
     * Get location details for a city to add to saved locations
     * @param cityName The city name to search for
     * @param callback Callback to handle the response
     */
    public void getLocationDetails(String cityName, final LocationSearchCallback callback) {
        // Search for the city
        searchLocationsByCity(cityName, new LocationSearchCallback() {
            @Override
            public void onSearchResults(List<Location> locations) {
                if (locations != null && !locations.isEmpty()) {
                    // Get the first result
                    Location location = locations.get(0);
                    
                    // Get current weather for this location
                    getCurrentConditions(location.getId(), new CurrentConditionsCallback() {
                        @Override
                        public void onCurrentConditions(String temperature, String condition, String highTemp, String lowTemp,
                                                      String feelsLike, String humidity, String wind, String precipitation,
                                                      String pressure, String visibility, String sunrise, String sunset) {
                            // Update location with weather data
                            location.setCondition(condition);
                            location.setTemperature(temperature);
                            
                            // Return the location
                            List<Location> result = new ArrayList<>();
                            result.add(location);
                            callback.onSearchResults(result);
                        }
                        
                        @Override
                        public void onError(String message) {
                            // Return the location without weather data
                            List<Location> result = new ArrayList<>();
                            result.add(location);
                            callback.onSearchResults(result);
                        }
                    });
                } else {
                    callback.onError("No locations found for: " + cityName);
                }
            }
            
            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
    
    /**
     * Save locations to SharedPreferences
     * @param locations The locations to save
     */
    public void saveLocations(List<Location> locations) {
        // Convert locations to JSON string
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            json.append("{\"id\":\"")
                .append(location.getId())
                .append("\",\"name\":\"")
                .append(location.getName())
                .append("\",\"condition\":\"")
                .append(location.getCondition())
                .append("\",\"temperature\":\"")
                .append(location.getTemperature())
                .append("\",\"current\":")
                .append(location.isCurrentLocation())
                .append("}");
            
            if (i < locations.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        
        // Save to SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SAVED_LOCATIONS, json.toString()).apply();
        
        Log.d(TAG, "Saved " + locations.size() + " locations");
    }
    
    /**
     * Load saved locations from SharedPreferences
     * @return List of saved locations
     */
    public List<Location> loadSavedLocations() {
        List<Location> locations = new ArrayList<>();
        
        // Load from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SAVED_LOCATIONS, "[]");
        
        // Parse JSON string
        try {
            // Simple parsing without using a JSON library
            if (json.length() > 2) { // More than just []
                String[] items = json.substring(1, json.length() - 1).split("\\},\\{");
                
                for (String item : items) {
                    // Clean up the item string
                    item = item.replace("{", "").replace("}", "");
                    
                    // Extract fields
                    String id = extractJsonField(item, "id");
                    String name = extractJsonField(item, "name");
                    String condition = extractJsonField(item, "condition");
                    String temperature = extractJsonField(item, "temperature");
                    boolean isCurrent = Boolean.parseBoolean(extractJsonField(item, "current"));
                    
                    // Create location object
                    Location location = new Location(id, name, condition, temperature, isCurrent);
                    locations.add(location);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing saved locations: " + e.getMessage());
        }
        
        return locations;
    }
    
    /**
     * Helper method to extract a field from a JSON string
     * @param json The JSON string
     * @param field The field name
     * @return The field value
     */
    private String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\":\"([^\"]*)\""; // For string values
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        
        if (m.find()) {
            return m.group(1);
        }
        
        // Try boolean pattern
        pattern = "\"" + field + "\":([^,}]*)"; // For boolean values
        r = java.util.regex.Pattern.compile(pattern);
        m = r.matcher(json);
        
        if (m.find()) {
            return m.group(1);
        }
        
        return "";
    }
    
    /**
     * Format temperature value based on user preferences
     * @param temp Temperature in Celsius (from API)
     * @return Formatted temperature string with appropriate unit
     */
    private String formatTemperature(double temp) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_APP_NAME, Context.MODE_PRIVATE);
        String unit = preferences.getString("temperature_unit", "celsius");
        
        if ("fahrenheit".equals(unit)) {
            // Convert Celsius to Fahrenheit
            temp = (temp * 9/5) + 32;
            return Math.round(temp) + "°F";
        } else {
            // Default to Celsius
            return Math.round(temp) + "°C";
        }
    }
    
    /**
     * Format wind speed based on user preferences
     * @param speed Wind speed in m/s (from API)
     * @return Formatted wind speed string with appropriate unit
     */
    private String formatWindSpeed(double speed) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_APP_NAME, Context.MODE_PRIVATE);
        String unit = preferences.getString("wind_speed_unit", "kmh");
        
        if ("kmh".equals(unit)) {
            // Convert m/s to km/h
            speed = speed * 3.6;
            return Math.round(speed) + " km/h";
        } else if ("mph".equals(unit)) {
            // Convert m/s to mph
            speed = speed * 2.23694;
            return Math.round(speed) + " mph";
        } else {
            // Default to m/s
            return Math.round(speed) + " m/s";
        }
    }
    
    /**
     * Format time from timestamp
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string (HH:mm)
     */
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format hour from timestamp
     * @param timestamp Timestamp in milliseconds
     * @return Formatted hour string (HH:mm)
     */
    private String formatHour(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Capitalize first letter of a string
     * @param input Input string
     * @return String with first letter capitalized
     */
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    
    /**
     * Get weather icon resource based on OpenWeatherMap icon code
     * @param iconCode OpenWeatherMap icon code
     * @return Resource ID for the corresponding weather icon
     */
    private int getWeatherIconResource(String iconCode) {
        // Map OpenWeatherMap icon codes to our drawable resources
        switch (iconCode) {
            case "01d": // Clear sky (day)
                return R.drawable.ph_sun;
            case "01n": // Clear sky (night)
                return R.drawable.ph_moon;
            case "02d": // Few clouds (day)
                return R.drawable.ph_cloud_sun;
            case "02n": // Few clouds (night)
                return R.drawable.ph_cloud_moon;
            case "03d": // Scattered clouds (day)
            case "03n": // Scattered clouds (night)
            case "04d": // Broken clouds (day)
            case "04n": // Broken clouds (night)
                return R.drawable.ph_cloud;
            case "09d": // Shower rain (day)
            case "09n": // Shower rain (night)
                return R.drawable.ph_cloud_rain;
            case "10d": // Rain (day)
                return R.drawable.ph_cloud_rain;
            case "10n": // Rain (night)
                return R.drawable.ph_cloud_rain;
            case "11d": // Thunderstorm (day)
            case "11n": // Thunderstorm (night)
                return R.drawable.ph_cloud_lightning;
            case "13d": // Snow (day)
            case "13n": // Snow (night)
                return R.drawable.ph_cloud_snow;
            case "50d": // Mist (day)
            case "50n": // Mist (night)
                return R.drawable.ph_cloud;
            default:
                return R.drawable.ph_cloud;
        }
    }
}
