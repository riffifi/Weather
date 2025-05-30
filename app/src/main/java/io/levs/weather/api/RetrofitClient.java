package io.levs.weather.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String OPENWEATHER_BASE_URL = "https://api.openweathermap.org/";
    private static Retrofit openWeatherRetrofit = null;
    
    private static OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }
    
    private static Retrofit getOpenWeatherClient() {
        if (openWeatherRetrofit == null) {
            openWeatherRetrofit = new Retrofit.Builder()
                    .baseUrl(OPENWEATHER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(createOkHttpClient())
                    .build();
        }
        return openWeatherRetrofit;
    }
    
    public static OpenWeatherService getOpenWeatherService() {
        return getOpenWeatherClient().create(OpenWeatherService.class);
    }
}