package io.levs.weather.api;

import java.util.List;

import io.levs.weather.models.openweather.OpenWeatherCurrentResponse;
import io.levs.weather.models.openweather.OpenWeatherForecastResponse;
import io.levs.weather.models.openweather.OpenWeatherLocationResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface OpenWeatherService {
    String API_KEY = io.levs.weather.BuildConfig.OPEN_WEATHER_API_KEY;
    
    /**
     * Search for locations by city name
     * @param query The city name to search for
     * @param limit Maximum number of results to return
     * @return A list of locations matching the query
     */
    @GET("geo/1.0/direct")
    Call<List<OpenWeatherLocationResponse>> searchLocationsByCity(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("appid") String apiKey);
    
    /**
     * Get current weather for a location
     * @param lat Latitude
     * @param lon Longitude
     * @param units Units (metric or imperial)
     * @return Current weather conditions
     */
    @GET("data/2.5/weather")
    Call<OpenWeatherCurrentResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("appid") String apiKey);
    
    /**
     * Get 5-day forecast for a location
     * @param lat Latitude
     * @param lon Longitude
     * @param units Units (metric or imperial)
     * @return 5-day forecast with 3-hour intervals
     */
    @GET("data/2.5/forecast")
    Call<OpenWeatherForecastResponse> getForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("appid") String apiKey);
}
