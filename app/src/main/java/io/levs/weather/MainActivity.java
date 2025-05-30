package io.levs.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import io.levs.weather.api.WeatherRepository;
import io.levs.weather.models.Location;
import io.levs.weather.utils.WeatherColorUtils;


import io.levs.weather.models.DailyForecast;
import io.levs.weather.models.HourlyForecast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION_SELECTION = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    

    private String locationKey = "328328";
    private String locationDisplayName = "London";
    private WeatherRepository repository;
    private FusedLocationProviderClient fusedLocationClient;
    private Vibrator vibrator;
    private BroadcastReceiver unitChangedReceiver;
    private TextView locationName;
    private TextView temperatureTextView;
    private TextView iconWeatherConditionTextView; // Text below the icon
    private TextView tempWeatherConditionTextView;
    private TextView highTempTextView;
    private TextView lowTempTextView;
    private RecyclerView hourlyForecastRecyclerView;
    private RecyclerView dailyForecastRecyclerView;
    private TextView showMoreForecastButton;
    private ImageButton menuButton;
    private ImageButton settingsButton;
    private View touchOverlay;
    
    // Card views for animations
    private MaterialCardView hourlyForecastCard;
    private MaterialCardView dailyForecastCard;
    private MaterialCardView weatherDetailsCard;
    
    // Bottom sheet
    private NestedScrollView contentScrollView;
    private BottomSheetBehavior<NestedScrollView> bottomSheetBehavior;
    private View weatherIconContainer;
    private ImageView weatherConditionIcon;
    

    private TextView feelsLikeTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView precipitationTextView;
    private TextView pressureTextView;
    private TextView visibilityTextView;
    private TextView sunriseTimeTextView;
    private TextView sunsetTimeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = WeatherRepository.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        setupUnitChangeReceiver();
        
        initViews();
        setupBottomSheet();
        setClickListeners();
        setupRecyclerViews();
        
        if (checkLocationPermission()) {
            getCurrentLocation();
        } else {
            loadWeatherData();
        }
        
        animateContent();
    }
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }
    
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("io.levs.weather.UNIT_CHANGED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(unitChangedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(unitChangedReceiver, filter);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // unregister the receiver
        try {
            unregisterReceiver(unitChangedReceiver);
        } catch (IllegalArgumentException e) {
            // ignore if not registered
        }
    }
    
    private void setupUnitChangeReceiver() {
        unitChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("io.levs.weather.UNIT_CHANGED")) {
                    String unitType = intent.getStringExtra("unit_type");
                    String newUnit = intent.getStringExtra("new_unit");
                    
                    if (unitType != null && newUnit != null) {
                        Log.d(TAG, "Unit changed: " + unitType + " to " + newUnit);
                        loadWeatherData();
                    }
                }
            }
        };
    }
    
    private void performHapticFeedback() {
        View view = getCurrentFocus();
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } else if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.EFFECT_CLICK));
            } else {
                vibrator.vibrate(20);
            }
        }
    }

    private void initViews() {
        locationName = findViewById(R.id.locationName);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        iconWeatherConditionTextView = findViewById(R.id.iconWeatherConditionTextView);
        tempWeatherConditionTextView = findViewById(R.id.tempWeatherConditionTextView);
        highTempTextView = findViewById(R.id.highTempTextView);
        lowTempTextView = findViewById(R.id.lowTempTextView);
        hourlyForecastRecyclerView = findViewById(R.id.hourlyForecastRecyclerView);
        dailyForecastRecyclerView = findViewById(R.id.dailyForecastRecyclerView);
        showMoreForecastButton = findViewById(R.id.showMoreForecastButton);
        menuButton = findViewById(R.id.menuButton);
        settingsButton = findViewById(R.id.settingsButton);
        touchOverlay = findViewById(R.id.touchOverlay);
        
        hourlyForecastCard = findViewById(R.id.hourlyForecastCard);
        dailyForecastCard = findViewById(R.id.dailyForecastCard);
        weatherDetailsCard = findViewById(R.id.weatherDetailsCard);
        
        feelsLikeTextView = findViewById(R.id.feelsLikeTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windTextView = findViewById(R.id.windTextView);
        precipitationTextView = findViewById(R.id.precipitationTextView);
        pressureTextView = findViewById(R.id.pressureTextView);
        visibilityTextView = findViewById(R.id.visibilityTextView);
        sunriseTimeTextView = findViewById(R.id.sunriseTimeTextView);
        sunsetTimeTextView = findViewById(R.id.sunsetTimeTextView);
        
        contentScrollView = findViewById(R.id.contentScrollView);
        weatherIconContainer = findViewById(R.id.weatherIconContainer);
        weatherConditionIcon = findViewById(R.id.weatherConditionIcon);

    }

    private void setClickListeners() {
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // yaaaay, spinny settings icon
                performHapticFeedback();
                Animation rotateAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_settings_icon);
                settingsButton.startAnimation(rotateAnimation);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        menuButton.setOnClickListener(v -> {
            performHapticFeedback();
            Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
            startActivityForResult(intent, REQUEST_LOCATION_SELECTION);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        showMoreForecastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHapticFeedback();
                if (dailyForecastRecyclerView.getAdapter() != null && dailyForecastRecyclerView.getAdapter().getItemCount() == 4) {
                    expandDailyForecast();
                } else {
                    collapseDailyForecast();
                }
            }
        });
    }

    private void setupRecyclerViews() {
        hourlyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        setupHourlyForecastAdapter();

        dailyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupDailyForecastAdapter(4);
    }

    private void setupHourlyForecastAdapter() {

        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        HourlyForecastAdapter adapter = new HourlyForecastAdapter(hourlyForecasts);
        hourlyForecastRecyclerView.setAdapter(adapter);
    }

    private void setupDailyForecastAdapter(int days) {
        loadDailyForecast(locationKey, days);
    }
    
    private void expandDailyForecast() {
        android.view.animation.LayoutAnimationController animation = 
                android.view.animation.AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide);
        
        dailyForecastRecyclerView.setLayoutAnimation(animation);
        
        setupDailyForecastAdapter(10);
        
        showMoreForecastButton.setText(getString(R.string.show_less_forecast));
        
        dailyForecastRecyclerView.scheduleLayoutAnimation();
    }
    
    private void collapseDailyForecast() {
        android.view.animation.LayoutAnimationController animation = 
                android.view.animation.AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slide);
        
        dailyForecastRecyclerView.setLayoutAnimation(animation);
        
        setupDailyForecastAdapter(4);
        
        showMoreForecastButton.setText(getString(R.string.show_more_forecast));
        
        dailyForecastRecyclerView.scheduleLayoutAnimation();
    }
    
    private String getWindUnitString(SharedPreferences preferences) {
        String windUnit = preferences.getString("wind_speed_unit", "kmh");
        switch (windUnit) {
            case "kmh":
                return "km/h";
            case "mph":
                return "mph";
            default:
                return "m/s";
        }
    }
    
    private void loadDailyForecast(String locationId, int days) {
        repository.getDailyForecast(locationId, days, new WeatherRepository.DailyForecastCallback() {
            @Override
            public void onDailyForecast(List<DailyForecast> dailyForecasts) {
                runOnUiThread(() -> {

                    DailyForecastAdapter adapter = new DailyForecastAdapter(dailyForecasts);
                    dailyForecastRecyclerView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    List<DailyForecast> emptyList = new ArrayList<>();
                    DailyForecastAdapter adapter = new DailyForecastAdapter(emptyList);
                    dailyForecastRecyclerView.setAdapter(adapter);
                });
            }
        });
    }

    private void loadWeatherData() {
        Log.d(TAG, "Loading weather data for location: " + locationKey);
        locationName.setText(locationDisplayName);
        
        // Get current unit preferences
        SharedPreferences preferences = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        String tempUnit = preferences.getString("temperature_unit", "celsius");
        String windUnit = preferences.getString("wind_speed_unit", "kmh");
        
        String tempPlaceholder = tempUnit.equals("fahrenheit") ? getString(R.string.default_temp_f) : getString(R.string.default_temp_c);
        String windPlaceholder = getString(R.string.default_wind_placeholder, getWindUnitString(preferences));
        
        temperatureTextView.setText(tempPlaceholder);
        iconWeatherConditionTextView.setText(getString(R.string.loading));
        tempWeatherConditionTextView.setText(getString(R.string.loading));
        highTempTextView.setText("--°");
        lowTempTextView.setText("--°");
        
        feelsLikeTextView.setText(tempPlaceholder);
        humidityTextView.setText(getString(R.string.default_humidity_value));
        windTextView.setText(windPlaceholder);
        precipitationTextView.setText(getString(R.string.default_precipitation_value));
        pressureTextView.setText(getString(R.string.default_pressure_value));
        visibilityTextView.setText(getString(R.string.default_visibility_value));
        sunriseTimeTextView.setText(getString(R.string.default_time_value));
        sunsetTimeTextView.setText(getString(R.string.default_time_value));
        
        repository.getCurrentConditions(locationKey, new WeatherRepository.CurrentConditionsCallback() {
            @Override
            public void onCurrentConditions(String temperature, String condition, String highTemp, 
                                          String lowTemp, String feelsLike, String humidity, 
                                          String wind, String precipitation, String pressure, 
                                          String visibility, String sunrise, String sunset) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Updating UI with weather condition: " + condition);
                    
                    temperatureTextView.setText(temperature);
                    iconWeatherConditionTextView.setText(condition);
                    tempWeatherConditionTextView.setText(condition);
                    highTempTextView.setText(highTemp);
                    lowTempTextView.setText(lowTemp);
                    
                    updateWeatherIcon(condition);
                    
                    feelsLikeTextView.setText(feelsLike);
                    humidityTextView.setText(humidity);
                    windTextView.setText(wind);
                    precipitationTextView.setText(precipitation);
                    pressureTextView.setText(pressure);
                    visibilityTextView.setText(visibility);
                    sunriseTimeTextView.setText(sunrise);
                    sunsetTimeTextView.setText(sunset);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading weather data: " + message);
                    iconWeatherConditionTextView.setText(getString(R.string.no_data_available));
                    tempWeatherConditionTextView.setText(getString(R.string.no_data_available));
                    temperatureTextView.setText(getString(R.string.default_temp));
                    highTempTextView.setText(getString(R.string.default_temp));
                    lowTempTextView.setText(getString(R.string.default_temp));
                });
            }
        });
        
        repository.getHourlyForecast(locationKey, new WeatherRepository.HourlyForecastCallback() {
            @Override
            public void onHourlyForecast(List<HourlyForecast> hourlyForecasts) {
                runOnUiThread(() -> {
                    HourlyForecastAdapter adapter = new HourlyForecastAdapter(hourlyForecasts);
                    hourlyForecastRecyclerView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hourlyForecastRecyclerView.setAdapter(new HourlyForecastAdapter(new ArrayList<>()));
                });
            }
        });
        
        loadDailyForecast(locationKey, 4);
    }
    
    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(contentScrollView);
        
        bottomSheetBehavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels / 6);
        bottomSheetBehavior.setSkipCollapsed(false);
        contentScrollView.setNestedScrollingEnabled(false);
        touchOverlay.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private static final float SWIPE_THRESHOLD = 50;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = startY - event.getY();
                        if (deltaY > SWIPE_THRESHOLD) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            return true;
                        } else if (deltaY < -SWIPE_THRESHOLD) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            return true;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        float finalDeltaY = startY - event.getY();
                        if (Math.abs(finalDeltaY) < SWIPE_THRESHOLD) {
                            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            } else {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            }
                        }
                        return true;
                }
                return false;
            }
        });
        
        View hourlyForecastHandle = findViewById(R.id.hourlyForecastHandle);
        
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    weatherIconContainer.setTranslationY(-getResources().getDisplayMetrics().heightPixels / 4);
                    animateConditionTextToTemp(1.0f);
                    contentScrollView.setNestedScrollingEnabled(true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    weatherIconContainer.setTranslationY(0);
                    animateConditionTextToTemp(0.0f);
                    contentScrollView.setNestedScrollingEnabled(false);
                } else if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    contentScrollView.setNestedScrollingEnabled(true);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                hourlyForecastHandle.setAlpha(0.3f + (slideOffset * 0.5f));
                
                float translationDistance = -slideOffset * (getResources().getDisplayMetrics().heightPixels / 4);
                weatherIconContainer.setTranslationY(translationDistance);
                
                float fadeThreshold = 0.2f;
                float fadeOutRate = 0.6f - fadeThreshold;
                
                if (slideOffset <= fadeThreshold) {
                    weatherIconContainer.setAlpha(1.0f);
                } else if (slideOffset >= fadeThreshold + fadeOutRate) {
                    weatherIconContainer.setAlpha(0.0f);
                } else {
                    float normalizedOffset = (slideOffset - fadeThreshold) / fadeOutRate;
                    float alpha = 1.0f - normalizedOffset;
                    weatherIconContainer.setAlpha(alpha);
                }
                
                animateConditionTextToTemp(slideOffset);
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_LOCATION_SELECTION && resultCode == RESULT_OK && data != null) {
            String newLocationId = data.getStringExtra("locationId");
            String newLocationName = data.getStringExtra("locationName");
            
            if (newLocationId != null && newLocationName != null) {
                locationKey = newLocationId;
                locationDisplayName = newLocationName;
                
                locationName.setText(locationDisplayName);
                
                // Save location key and name to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
                prefs.edit()
                    .putString("last_location_key", locationKey)
                    .putString("last_location_name", locationDisplayName)
                    .apply();
                
                resetWeatherDetailsCard();
                
                loadWeatherData();
            }
        }
    }
    
    private void resetWeatherDetailsCard() {
        temperatureTextView.setText(getString(R.string.default_temp));
        iconWeatherConditionTextView.setText(getString(R.string.loading));
        tempWeatherConditionTextView.setText(getString(R.string.loading));
        highTempTextView.setText(getString(R.string.default_temp));
        lowTempTextView.setText(getString(R.string.default_temp));
        feelsLikeTextView.setText(getString(R.string.default_temp));
        humidityTextView.setText(getString(R.string.default_humidity_value));
        windTextView.setText(getString(R.string.default_wind_value));
        precipitationTextView.setText(getString(R.string.default_precipitation_value));
        pressureTextView.setText(getString(R.string.default_pressure_value));
        visibilityTextView.setText(getString(R.string.default_visibility_value));
        sunriseTimeTextView.setText(getString(R.string.default_time_value));
        sunsetTimeTextView.setText(getString(R.string.default_time_value));
        
        if (weatherConditionIcon != null) {
            weatherConditionIcon.setImageResource(R.drawable.ph_sun);
        }
        

    }
    

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }
    

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            
            resetWeatherDetailsCard();
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            if (location != null) {
                                String locationId = location.getLatitude() + "," + location.getLongitude();
                                
                                Log.d(TAG, "Got current location: " + locationId);
                                
                                locationKey = locationId;
                                locationName.setText(getString(R.string.loading));
                                
                                String[] latLon = locationId.split(",");
                                if (latLon.length == 2) {
                                    try {
                                        double lat = Double.parseDouble(latLon[0]);
                                        double lon = Double.parseDouble(latLon[1]);
                                        
                                        repository.getOpenWeatherService().getCurrentWeather(lat, lon, "metric", io.levs.weather.api.OpenWeatherService.API_KEY)
                                                .enqueue(new retrofit2.Callback<io.levs.weather.models.openweather.OpenWeatherCurrentResponse>() {
                                                    @Override
                                                    public void onResponse(retrofit2.Call<io.levs.weather.models.openweather.OpenWeatherCurrentResponse> call, 
                                                                           retrofit2.Response<io.levs.weather.models.openweather.OpenWeatherCurrentResponse> response) {
                                                        if (response.isSuccessful() && response.body() != null) {
                                                            String cityName = response.body().getName();
                                                            String countryCode = response.body().getSys() != null ? response.body().getSys().getCountry() : "";
                                                            
                                                            if (cityName != null && !cityName.isEmpty()) {
                                                                String displayName = countryCode != null && !countryCode.isEmpty() ? 
                                                                        cityName + ", " + countryCode : cityName;
                                                                locationDisplayName = displayName;
                                                                locationName.setText(displayName);
                                                            } else {
                                                                locationDisplayName = getString(R.string.current_location);
                                                                locationName.setText(locationDisplayName);
                                                            }
                                                        } else {
                                                            locationDisplayName = getString(R.string.current_location);
                                                            locationName.setText(locationDisplayName);
                                                        }
                                                    }
                                                    
                                                    @Override
                                                    public void onFailure(retrofit2.Call<io.levs.weather.models.openweather.OpenWeatherCurrentResponse> call, Throwable t) {
                                                        locationDisplayName = getString(R.string.current_location);
                                                        locationName.setText(locationDisplayName);
                                                    }
                                                });
                                    } catch (NumberFormatException e) {
                                        locationDisplayName = getString(R.string.current_location);
                                        locationName.setText(locationDisplayName);
                                    }
                                } else {
                                    locationDisplayName = getString(R.string.current_location);
                                    locationName.setText(locationDisplayName);
                                }
                                
                                // Save location key and name to SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
                                prefs.edit()
                                    .putString("last_location_key", locationKey)
                                    .putString("last_location_name", locationDisplayName)
                                    .apply();
                                
                                loadWeatherData();
                            } else {
                                Log.d(TAG, "Current location was null, using default location");
                                Toast.makeText(MainActivity.this, 
                                        getString(R.string.location_error), 
                                        Toast.LENGTH_SHORT).show();
                                loadWeatherData();
                            }
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Log.e(TAG, "Error getting location", e);
                        Toast.makeText(MainActivity.this, 
                                getString(R.string.location_error_with_message, e.getMessage()), 
                                Toast.LENGTH_SHORT).show();
                        loadWeatherData();
                    });
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied_toast), Toast.LENGTH_SHORT).show();
                loadWeatherData();
            }
        }
    }
    
    private void animateContent() {
        hourlyForecastCard.setVisibility(View.INVISIBLE);
        dailyForecastCard.setVisibility(View.INVISIBLE);
        weatherDetailsCard.setVisibility(View.INVISIBLE);
        
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        new Handler().postDelayed(() -> {
            hourlyForecastCard.setVisibility(View.VISIBLE);
            hourlyForecastCard.startAnimation(slideUpAnimation);
        }, 100);
        
        new Handler().postDelayed(() -> {
            dailyForecastCard.setVisibility(View.VISIBLE);
            dailyForecastCard.startAnimation(slideUpAnimation);
        }, 200);
        
        new Handler().postDelayed(() -> {
            weatherDetailsCard.setVisibility(View.VISIBLE);
            weatherDetailsCard.startAnimation(slideUpAnimation);
        }, 300);
    }

    private void updateWeatherIcon(String condition) {
        Log.d(TAG, "Updating weather icon for condition: " + condition);
        
        int iconResource = R.drawable.ph_sun; // Default to sun
        
        if (condition == null) {
            weatherConditionIcon.setImageResource(iconResource);
            return;
        }
        
        // Normalize condition text
        String lowerCondition = condition.toLowerCase().trim();
        
        if (lowerCondition.contains("cloud") && lowerCondition.contains("sun")) {
            iconResource = R.drawable.ph_cloud_sun;
        } else if (lowerCondition.contains("cloud") && lowerCondition.contains("moon")) {
            iconResource = R.drawable.ph_cloud_moon;
        } else if (lowerCondition.contains("cloud") && lowerCondition.contains("rain")) {
            iconResource = R.drawable.ph_cloud_rain;
        } else if (lowerCondition.contains("cloud") && lowerCondition.contains("snow")) {
            iconResource = R.drawable.ph_cloud_snow;
        } else if (lowerCondition.contains("cloud") && lowerCondition.contains("lightning")) {
            iconResource = R.drawable.ph_cloud_lightning;
        } else if (lowerCondition.contains("thunderstorm")) {
            iconResource = R.drawable.ph_cloud_lightning;
        } else if (lowerCondition.contains("rain") || lowerCondition.contains("shower") || lowerCondition.contains("drizzle")) {
            iconResource = R.drawable.ph_cloud_rain;
        } else if (lowerCondition.contains("snow") || lowerCondition.contains("sleet") || lowerCondition.contains("ice")) {
            iconResource = R.drawable.ph_cloud_snow;
        } else if (lowerCondition.contains("cloud") || lowerCondition.contains("overcast") || 
                  lowerCondition.contains("fog") || lowerCondition.contains("mist")) {
            iconResource = R.drawable.ph_cloud;
        } else if (lowerCondition.contains("clear") && lowerCondition.contains("night")) {
            iconResource = R.drawable.ph_moon;
        } else if (lowerCondition.contains("sun") || lowerCondition.contains("clear") || lowerCondition.contains("fair")) {
            iconResource = R.drawable.ph_sun;
        }
        
        weatherConditionIcon.setImageResource(iconResource);

    }
    
    private void animateConditionTextToTemp(float progress) {
        // Only proceed if the text views are available
        if (iconWeatherConditionTextView == null || tempWeatherConditionTextView == null) {
            return;
        }
        
        // Sync the text content between the two text views
        if (iconWeatherConditionTextView.getText() != null) {
            tempWeatherConditionTextView.setText(iconWeatherConditionTextView.getText());
        }
        
        // For the icon condition text, we just want to fade it out with the icon
        // Start fading out after 20% of the slide and be completely faded by 60%
        float fadeThreshold = 0.2f;
        float fadeOutRate = 0.6f - fadeThreshold;
        
        if (progress <= fadeThreshold) {
            // Keep fully visible until threshold
            iconWeatherConditionTextView.setAlpha(1.0f);
        } else if (progress >= fadeThreshold + fadeOutRate) {
            // Keep fully invisible after complete fade
            iconWeatherConditionTextView.setAlpha(0.0f);
        } else {
            // Gradually fade out between threshold and complete fade
            float normalizedOffset = (progress - fadeThreshold) / fadeOutRate;
            iconWeatherConditionTextView.setAlpha(1.0f - normalizedOffset);
        }
        
        // For the temperature condition text, we want to fade it in as we scroll
        // Start fading in after 50% of the slide and be completely visible by 90%
        float appearThreshold = 0.5f;
        float fadeInRate = 0.9f - appearThreshold;
        
        if (progress <= appearThreshold) {
            // Keep fully invisible until threshold
            tempWeatherConditionTextView.setAlpha(0.0f);
        } else if (progress >= appearThreshold + fadeInRate) {
            // Keep fully visible after complete fade in
            tempWeatherConditionTextView.setAlpha(1.0f);
        } else {
            // Gradually fade in between threshold and complete fade in
            float normalizedOffset = (progress - appearThreshold) / fadeInRate;
            tempWeatherConditionTextView.setAlpha(normalizedOffset);
        }
    }
}
