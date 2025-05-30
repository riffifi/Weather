package io.levs.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WeatherAppPreferences";
    private SharedPreferences preferences;

    private ImageButton backButton;
    private RadioGroup temperatureUnitRadioGroup;
    private RadioGroup windSpeedUnitRadioGroup;
    private MaterialRadioButton celsiusRadioButton;
    private MaterialRadioButton fahrenheitRadioButton;
    private MaterialRadioButton kmhRadioButton;
    private MaterialRadioButton mphRadioButton;
    private MaterialRadioButton msRadioButton;
    private MaterialCardView manageLocationsCard;
    private TextView appVersionTextView;
    
    private SwitchMaterial useCurrentLocationToggle;
    
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Get vibrator service using the appropriate API based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        initViews();
        backButton.setOnClickListener(v -> {
            finish();
            // Apply left-to-right slide animation when returning from settings
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        setupClickListeners();
        setupInitialValues();
    }
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }
    
    @Override
    public void finish() {
        super.finish();
        // Apply left-to-right slide animation when returning from settings
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        temperatureUnitRadioGroup = findViewById(R.id.temperatureUnitRadioGroup);
        windSpeedUnitRadioGroup = findViewById(R.id.windSpeedUnitRadioGroup);
        celsiusRadioButton = findViewById(R.id.celsiusRadioButton);
        fahrenheitRadioButton = findViewById(R.id.fahrenheitRadioButton);
        kmhRadioButton = findViewById(R.id.kmhRadioButton);
        mphRadioButton = findViewById(R.id.mphRadioButton);
        msRadioButton = findViewById(R.id.msRadioButton);
        manageLocationsCard = findViewById(R.id.manageLocationsCard);
        appVersionTextView = findViewById(R.id.appVersionTextView);
        
        useCurrentLocationToggle = findViewById(R.id.useCurrentLocationToggle);
    }

    private void setupClickListeners() {
        temperatureUnitRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                performHapticFeedback();
                
                if (checkedId == R.id.celsiusRadioButton) {
                    preferences.edit().putString("temperature_unit", "celsius").apply();
                    convertTemperatureUnits("celsius");
                } else if (checkedId == R.id.fahrenheitRadioButton) {
                    preferences.edit().putString("temperature_unit", "fahrenheit").apply();
                    convertTemperatureUnits("fahrenheit");
                }
            }
        });
        
        windSpeedUnitRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                performHapticFeedback();
                
                if (checkedId == R.id.kmhRadioButton) {
                    preferences.edit().putString("wind_speed_unit", "kmh").apply();
                    convertWindSpeedUnits("kmh");
                } else if (checkedId == R.id.mphRadioButton) {
                    preferences.edit().putString("wind_speed_unit", "mph").apply();
                    convertWindSpeedUnits("mph");
                } else if (checkedId == R.id.msRadioButton) {
                    preferences.edit().putString("wind_speed_unit", "ms").apply();
                    convertWindSpeedUnits("ms");
                }
            }
        });
        
        manageLocationsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHapticFeedback();
                startActivity(new Intent(SettingsActivity.this, LocationsActivity.class));
            }
        });
        
        setupSwitchWithHapticFeedback(useCurrentLocationToggle, "use_current_location");
    }

    private void setupInitialValues() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            appVersionTextView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            appVersionTextView.setText("1.0.0");
        }
        
        String temperatureUnit = preferences.getString("temperature_unit", "celsius");
        if (temperatureUnit.equals("celsius")) {
            celsiusRadioButton.setChecked(true);
        } else {
            fahrenheitRadioButton.setChecked(true);
        }
        
        String windSpeedUnit = preferences.getString("wind_speed_unit", "kmh");
        if (windSpeedUnit.equals("kmh")) {
            kmhRadioButton.setChecked(true);
        } else if (windSpeedUnit.equals("mph")) {
            mphRadioButton.setChecked(true);
        } else {
            msRadioButton.setChecked(true);
        }
        
        useCurrentLocationToggle.setChecked(preferences.getBoolean("use_current_location", true));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setupSwitchWithHapticFeedback(SwitchMaterial switchView, String preferenceKey) {
        if (switchView == null) return;
        
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                performHapticFeedback();
                preferences.edit().putBoolean(preferenceKey, isChecked).apply();
            }
        });
    }
    
    private void convertTemperatureUnits(String newUnit) {
        String previousUnit = preferences.getString("temperature_unit", "celsius");
        if (previousUnit.equals(newUnit)) return;
        
        // We don't need to manually convert stored temperatures anymore
        // since the repository will handle this based on user preferences
        
        // Clear any cached weather data to force a refresh with new units
        preferences.edit().remove("weather_data_cache").apply();
        
        // Show a toast to confirm the change
        Toast.makeText(this, "Temperature unit changed to " + 
                      (newUnit.equals("celsius") ? "Celsius" : "Fahrenheit"), 
                      Toast.LENGTH_SHORT).show();
        
        // Send broadcast to notify MainActivity of the unit change
        Intent updateIntent = new Intent("io.levs.weather.UNIT_CHANGED");
        updateIntent.putExtra("unit_type", "temperature");
        updateIntent.putExtra("new_unit", newUnit);
        sendBroadcast(updateIntent);
    }
    
    private void convertWindSpeedUnits(String newUnit) {
        String previousUnit = preferences.getString("wind_speed_unit", "kmh");
        if (previousUnit.equals(newUnit)) return;
        
        // We don't need to manually convert stored wind speeds anymore
        // since the repository will handle this based on user preferences
        
        // Clear any cached weather data to force a refresh with new units
        preferences.edit().remove("weather_data_cache").apply();
        
        // Show a toast to confirm the change
        String unitName = "";
        switch (newUnit) {
            case "kmh":
                unitName = "km/h";
                break;
            case "mph":
                unitName = "mph";
                break;
            case "ms":
                unitName = "m/s";
                break;
        }
        
        Toast.makeText(this, "Wind speed unit changed to " + unitName, Toast.LENGTH_SHORT).show();
        
        // Send broadcast to notify MainActivity of the unit change
        Intent updateIntent = new Intent("io.levs.weather.UNIT_CHANGED");
        updateIntent.putExtra("unit_type", "wind_speed");
        updateIntent.putExtra("new_unit", newUnit);
        sendBroadcast(updateIntent);
    }
    
    private void performHapticFeedback() {
        View view = getCurrentFocus();
        if (view != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use modern haptic constants for Android 13+
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } else if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use predefined effects for Android 13+
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0+ but below 13
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.EFFECT_CLICK));
            } else {
                // Fallback for older devices
                vibrator.vibrate(20);
            }
        }
    }
}
