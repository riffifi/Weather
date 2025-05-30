package io.levs.weather.api;


// TODO: migrate from this to OpenWeatherRepository
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 * Class to handle all weather data operations
 */
public class WeatherRepository {
    private static final String TAG = "WeatherRepository";
    private static final String PREF_NAME = "weather_prefs";
    private static final String KEY_SAVED_LOCATIONS = "saved_locations";
    private static final String KEY_CURRENT_LOCATION = "current_location";
    
    private static WeatherRepository instance;
    private final OpenWeatherService openWeatherService;
    private final Context context;
    
    private WeatherRepository(Context context) {
        this.context = context.getApplicationContext();
        openWeatherService = RetrofitClient.getOpenWeatherService();
    }
    
    /**
     * Get the OpenWeatherService instance
     * @return OpenWeatherService instance
     */
    public OpenWeatherService getOpenWeatherService() {
        return openWeatherService;
    }
    
    public static synchronized WeatherRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherRepository(context);
        }
        return instance;
    }
    
    /**
     * Interface for callback when search results are ready
     */
    public interface LocationSearchCallback {
        void onSearchResults(List<Location> locations);
        void onError(String message);
    }
    
    /**
     * Interface for callback when current conditions are ready
     */
    public interface CurrentConditionsCallback {
        void onCurrentConditions(String temperature, String condition, String highTemp, String lowTemp,
                                String feelsLike, String humidity, String wind, String precipitation,
                                String pressure, String visibility, String sunrise, String sunset);
        void onError(String message);
    }
    
    /**
     * Interface for callback when hourly forecast is ready
     */
    public interface HourlyForecastCallback {
        void onHourlyForecast(List<HourlyForecast> hourlyForecasts);
        void onError(String message);
    }
    
    /**
     * Interface for callback when daily forecast is ready
     */
    public interface DailyForecastCallback {
        void onDailyForecast(List<DailyForecast> dailyForecasts);
        void onError(String message);
    }
    
    /**
     * Search for locations by city name
     */
    public void searchLocationsByCity(String query, final LocationSearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onError("Search query cannot be empty");
            return;
        }
        
        openWeatherService.searchLocationsByCity(query, 5, OpenWeatherService.API_KEY)
            .enqueue(new Callback<List<OpenWeatherLocationResponse>>() {
                @Override
                public void onResponse(Call<List<OpenWeatherLocationResponse>> call, 
                                       Response<List<OpenWeatherLocationResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final List<Location> locations = new ArrayList<>();
                        final int[] locationsToProcess = {response.body().size()};
                        
                        if (locationsToProcess[0] == 0) {
                            callback.onSearchResults(locations);
                            return;
                        }
                        
                        for (OpenWeatherLocationResponse locationResponse : response.body()) {
                            final String locationId = locationResponse.getLat() + "," + locationResponse.getLon();
                            final String locationName = locationResponse.getFullName();
                            
                            // Fetch current weather for this location
                            try {
                                double lat = locationResponse.getLat();
                                double lon = locationResponse.getLon();
                                
                                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                String units = prefs.getString("units", "metric");
                                
                                openWeatherService.getCurrentWeather(lat, lon, units, OpenWeatherService.API_KEY)
                                    .enqueue(new Callback<OpenWeatherCurrentResponse>() {
                                        @Override
                                        public void onResponse(Call<OpenWeatherCurrentResponse> call, 
                                                               Response<OpenWeatherCurrentResponse> response) {
                                            String temperature = "--°";
                                            String condition = "Unknown";
                                            
                                            if (response.isSuccessful() && response.body() != null) {
                                                OpenWeatherCurrentResponse data = response.body();
                                                temperature = formatTemperature(data.getMain().getTemp(), units);
                                                condition = data.getWeather().get(0).getDescription();
                                                condition = condition.substring(0, 1).toUpperCase() + condition.substring(1);
                                            }
                                            
                                            locations.add(new Location(
                                                locationId,
                                                locationName,
                                                condition, 
                                                temperature, 
                                                false
                                            ));
                                            
                                            locationsToProcess[0]--;
                                            if (locationsToProcess[0] <= 0) {
                                                callback.onSearchResults(locations);
                                            }
                                        }
                                        
                                        @Override
                                        public void onFailure(Call<OpenWeatherCurrentResponse> call, Throwable t) {
                                            locations.add(new Location(
                                                locationId,
                                                locationName,
                                                "Unknown", 
                                                "--°", 
                                                false
                                            ));
                                            
                                            locationsToProcess[0]--;
                                            if (locationsToProcess[0] <= 0) {
                                                callback.onSearchResults(locations);
                                            }
                                        }
                                    });
                            } catch (Exception e) {
                                locations.add(new Location(
                                    locationId,
                                    locationName,
                                    "Unknown", 
                                    "--°", 
                                    false
                                ));
                                
                                locationsToProcess[0]--;
                                if (locationsToProcess[0] <= 0) {
                                    callback.onSearchResults(locations);
                                }
                            }
                        }
                    } else {
                        callback.onError("Failed to find locations: " + 
                                        (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    }
                }
                
                @Override
                public void onFailure(Call<List<OpenWeatherLocationResponse>> call, Throwable t) {
                    callback.onError("Network error: " + t.getMessage());
                }
            });
    }
    
    /**
     * Get current conditions for a location 
     */
    public void getCurrentConditions(String locationKey, final CurrentConditionsCallback callback) {
        if (locationKey == null || locationKey.trim().isEmpty()) {
            callback.onError("Location key cannot be empty");
            return;
        }
        
        // format "lat,lon"
        String[] latLon = locationKey.split(",");
        if (latLon.length != 2) {
            callback.onError("Invalid location key format");
            return;
        }
        
        try {
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String units = prefs.getString("units", "metric");
            
            openWeatherService.getCurrentWeather(lat, lon, units, OpenWeatherService.API_KEY)
                .enqueue(new Callback<OpenWeatherCurrentResponse>() {
                    @Override
                    public void onResponse(Call<OpenWeatherCurrentResponse> call, 
                                           Response<OpenWeatherCurrentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenWeatherCurrentResponse data = response.body();
                            
                            String temperature = formatTemperature(data.getMain().getTemp(), units);
                            String condition = data.getWeather().get(0).getDescription();
                            condition = condition.substring(0, 1).toUpperCase() + condition.substring(1);
                            
                            String highTemp = formatTemperature(data.getMain().getTempMax(), units);
                            String lowTemp = formatTemperature(data.getMain().getTempMin(), units);
                            String feelsLike = formatTemperature(data.getMain().getFeelsLike(), units);
                            String humidity = data.getMain().getHumidity() + "%";
                            
                            String windUnit = units.equals("imperial") ? "mph" : "km/h";
                            String wind = formatWind(data.getWind().getSpeed(), units) + " " + windUnit;

                            String precipitation = "0%";
                            if (data.getRain() != null && data.getRain().getOneHour() > 0) {
                                precipitation = "100%"; 
                            }
                            
                            String pressure = data.getMain().getPressure() + " hPa";
                            String visibility = (data.getVisibility() / 1000) + " km";
                            
                            String sunrise = formatTime(data.getSys().getSunrise() * 1000); // to ms
                            String sunset = formatTime(data.getSys().getSunset() * 1000); // to ms
                            
                            callback.onCurrentConditions(
                                    temperature,
                                    condition,
                                    highTemp,
                                    lowTemp,
                                    feelsLike,
                                    humidity,
                                    wind,
                                    precipitation,
                                    pressure,
                                    visibility,
                                    sunrise,
                                    sunset
                            );
                        } else {
                            callback.onError("Failed to get weather data: " + 
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
     * Format temp
     */
    private String formatTemperature(double temp, String units) {
        return Math.round(temp) + (units.equals("imperial") ? "°F" : "°C");
    }
    
    /**
     * Format wind speed
     */
    private String formatWind(double speed, String units) {

        if (units.equals("metric")) {
            speed = speed * 3.6; // ms to kmh
        }
        return String.format(Locale.US, "%.1f", speed);
    }
    
    /**
     * Get hourly forecast for a location
     */
    public void getHourlyForecast(String locationKey, final HourlyForecastCallback callback) {
        if (locationKey == null || locationKey.trim().isEmpty()) {
            callback.onError("Location key cannot be empty");
            return;
        }
        
        String[] latLon = locationKey.split(",");
        if (latLon.length != 2) {
            callback.onError("Invalid location key format");
            return;
        }
        
        try {
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String units = prefs.getString("units", "metric"); // default - metric
            
            openWeatherService.getForecast(lat, lon, units, OpenWeatherService.API_KEY)
                .enqueue(new Callback<OpenWeatherForecastResponse>() {
                    @Override
                    public void onResponse(Call<OpenWeatherForecastResponse> call, 
                                           Response<OpenWeatherForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenWeatherForecastResponse data = response.body();
                            List<HourlyForecast> hourlyForecasts = new ArrayList<>();
                            int count = Math.min(24, data.getList().size());
                            for (int i = 0; i < count; i++) {
                                OpenWeatherForecastResponse.ForecastItem item = data.getList().get(i);
                                String time = formatHourTime(item.getDtTxt());
                                String temp = formatTemperature(item.getMain().getTemp(), units);
                                
                                // map conditions to icons
                                int weatherId = item.getWeather().get(0).getId();
                                boolean isDaylight = true;
                                int iconResource = getWeatherIconResource(weatherId, isDaylight);
                                
                                hourlyForecasts.add(new HourlyForecast(time, temp, iconResource));
                            }
                            
                            callback.onHourlyForecast(hourlyForecasts);
                        } else {
                            callback.onError("Failed to get forecast data: " + 
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
     * Get daily forecast
     */
    public void getDailyForecast(String locationKey, int days, final DailyForecastCallback callback) {
        if (locationKey == null || locationKey.trim().isEmpty()) {
            callback.onError("Location key cannot be empty");
            return;
        }
        String[] latLon = locationKey.split(",");
        if (latLon.length != 2) {
            callback.onError("Invalid location key format");
            return;
        }
        
        try {
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String units = prefs.getString("units", "metric"); 
            
            openWeatherService.getForecast(lat, lon, units, OpenWeatherService.API_KEY)
                .enqueue(new Callback<OpenWeatherForecastResponse>() {
                    @Override
                    public void onResponse(Call<OpenWeatherForecastResponse> call, 
                                           Response<OpenWeatherForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenWeatherForecastResponse data = response.body();
                            List<DailyForecast> dailyForecasts = new ArrayList<>();
                            
                            // mapping temps
                            Map<String, Double> dailyHighTemps = new HashMap<>();
                            Map<String, Double> dailyLowTemps = new HashMap<>();
                            Map<String, String> dailyConditions = new HashMap<>();
                            Map<String, Integer> dailyIcons = new HashMap<>();
                            
                            for (OpenWeatherForecastResponse.ForecastItem item : data.getList()) {
                                // format: "yyyy-MM-dd HH:mm:ss"
                                String date = item.getDtTxt().split(" ")[0];
                                double temp = item.getMain().getTemp();
                                if (!dailyHighTemps.containsKey(date) || temp > dailyHighTemps.get(date)) {
                                    dailyHighTemps.put(date, temp);
                                }
                                
                                if (!dailyLowTemps.containsKey(date) || temp < dailyLowTemps.get(date)) {
                                    dailyLowTemps.put(date, temp);
                                }
                                
                                if (!dailyConditions.containsKey(date) || item.getDtTxt().contains("12:00")) {
                                    dailyConditions.put(date, item.getWeather().get(0).getDescription());
                                    int weatherId = item.getWeather().get(0).getId();
                                    boolean isDaylight = true;
                                    dailyIcons.put(date, getWeatherIconResource(weatherId, isDaylight));
                                }
                            }
                            
                            List<String> sortedDates = new ArrayList<>(dailyHighTemps.keySet());
                            Collections.sort(sortedDates);
                            
                            // create daily forecasts
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            String today = dateFormat.format(calendar.getTime());
                            
                            int daysAdded = 0;
                            for (String date : sortedDates) {
                                if (daysAdded >= days) break;

                                String dayName;
                                if (date.equals(today)) {
                                    dayName = "Today";
                                } else {
                                    try {
                                        Date forecastDate = dateFormat.parse(date);
                                        Calendar forecastCal = Calendar.getInstance();
                                        forecastCal.setTime(forecastDate);
                                        
                                        if (daysAdded == 1) {
                                            dayName = "Tomorrow";
                                        } else {
                                            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
                                            dayName = dayFormat.format(forecastDate);
                                        }
                                    } catch (Exception e) {
                                        dayName = "Day " + (daysAdded + 1);
                                    }
                                }
                                
                                String highTemp = formatTemperature(dailyHighTemps.get(date), units);
                                String lowTemp = formatTemperature(dailyLowTemps.get(date), units);
                                
                                int icon = dailyIcons.getOrDefault(date, R.drawable.ph_cloud_sun);
                                String condition = dailyConditions.get(date);
                                if (condition != null && !condition.isEmpty()) {
                                    condition = condition.substring(0, 1).toUpperCase() + condition.substring(1);
                                }
                                
                                dailyForecasts.add(new DailyForecast(dayName, highTemp, lowTemp, icon));
                                daysAdded++;
                            }
                            
                            callback.onDailyForecast(dailyForecasts);
                        } else {
                            callback.onError("Failed to get forecast data: " + 
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
     */
    public void getLocationDetails(String cityName, final LocationSearchCallback callback) {
        if (cityName == null || cityName.trim().isEmpty()) {
            callback.onError("City name cannot be empty");
            return;
        }
        
        searchLocationsByCity(cityName, callback);
    }
    
    public void saveLocations(List<Location> locations) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            if (location == null) continue;
            
            // Format: id|name|condition|temperature|isCurrentLocation
            sb.append(location.getId()).append("|");
            sb.append(location.getName()).append("|");
            sb.append(location.getCondition()).append("|");
            sb.append(location.getTemperature()).append("|");
            sb.append(location.isCurrentLocation() ? "1" : "0");
            
            if (i < locations.size() - 1) {
                sb.append("###");
            }
        }
        
        Log.d(TAG, "Saving locations: " + sb.toString());
        editor.putString(KEY_SAVED_LOCATIONS, sb.toString());
        editor.apply();
    }
    

    public List<Location> loadSavedLocations() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedLocationsStr = prefs.getString(KEY_SAVED_LOCATIONS, "");
        
        List<Location> locations = new ArrayList<>();
        
        if (savedLocationsStr != null && !savedLocationsStr.isEmpty()) {
            Log.d(TAG, "Loading saved locations: " + savedLocationsStr);
            
            try {
                // Check if this is using the new ### delimiter format
                if (savedLocationsStr.contains("###")) {
                    String[] locationStrings = savedLocationsStr.split("###");
                    
                    for (String locationStr : locationStrings) {
                        parseAndAddLocation(locationStr, locations);
                    }
                } 
                else if (savedLocationsStr.contains(",")) {
                    if (parseAndAddLocation(savedLocationsStr, locations)) {
                    } else {
                        String[] locationStrings = savedLocationsStr.split(",");
                        for (String locationStr : locationStrings) {
                            parseAndAddLocation(locationStr, locations);
                        }
                    }
                } else {
                    parseAndAddLocation(savedLocationsStr, locations);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing saved locations: " + e.getMessage());
            }
        }
        
        Log.d(TAG, "Loaded " + locations.size() + " locations");
        return locations;
    }
    
    /**
     * Parse a location string and add it to the locations list
     * @param locationStr The location string to parse
     * @param locations The list to add the parsed location to
     * @return true if the location was successfully parsed and added, false otherwise
     */
    private boolean parseAndAddLocation(String locationStr, List<Location> locations) {
        if (locationStr == null || locationStr.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = locationStr.split("\\|");
        if (parts.length >= 5) {
            String id = parts[0];
            String name = parts[1];
            String condition = parts[2];
            String temperature = parts[3];
            boolean isCurrentLocation = "1".equals(parts[4]);
            
            Location location = new Location(id, name, condition, temperature, isCurrentLocation);
            locations.add(location);
            Log.d(TAG, "Loaded location: " + name + ", ID: " + id);
            return true;
        } else {
            Log.e(TAG, "Invalid location format: " + locationStr + ", parts: " + parts.length);
            return false;
        }
    }
    
    private int getWeatherIconResource(int weatherIcon, boolean isDaylight) {
        // Map OW icon codes to app icons
        
        // thunderstorm
        if (weatherIcon >= 200 && weatherIcon < 300) {
            return R.drawable.ph_cloud_lightning;
        }
        
        // drizzle/rain
        if ((weatherIcon >= 300 && weatherIcon < 400) || (weatherIcon >= 500 && weatherIcon < 600)) {
            return R.drawable.ph_cloud_rain;
        }
        
        // snow
        if (weatherIcon >= 600 && weatherIcon < 700) {
            return R.drawable.ph_cloud_snow;
        }
        
        // atmosphere (fog, mist, others)
        if (weatherIcon >= 700 && weatherIcon < 800) {
            return R.drawable.ph_cloud;
        }
        
        // clear
        if (weatherIcon == 800) {
            return isDaylight ? R.drawable.ph_sun : R.drawable.ph_moon;
        }
        
        // clouds
        if (weatherIcon > 800 && weatherIcon < 900) {
            if (weatherIcon == 801) {
                return isDaylight ? R.drawable.ph_cloud_sun : R.drawable.ph_cloud_moon;
            } else {
                return R.drawable.ph_cloud;
            }
        }
        
        // default
        return isDaylight ? R.drawable.ph_cloud_sun : R.drawable.ph_cloud_moon;
    }
    
    /**
     * format from iso date string
     */
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * format hour from ISO date string
     */
    private String formatHourTime(String isoDateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("h a", Locale.US);
            Date date = inputFormat.parse(isoDateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * format day of week from ISO date string
     */
    private String formatDayOfWeek(String isoDateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE", Locale.US);
            Date date = inputFormat.parse(isoDateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}
