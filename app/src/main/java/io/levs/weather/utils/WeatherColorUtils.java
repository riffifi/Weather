package io.levs.weather.utils;

import android.graphics.Color;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to determine colors based on weather conditions and time of day
 */
public class WeatherColorUtils {

    // Time of day color modifiers
    private static final String MORNING_MODIFIER = "#FFF4D0"; // Warm yellow tint
    private static final String DAY_MODIFIER = "#FFFFFF";     // Pure white (no tint)
    private static final String EVENING_MODIFIER = "#FFB74D"; // Orange tint
    private static final String NIGHT_MODIFIER = "#3F51B5";   // Deep blue tint

    // Base colors for different weather conditions
    private static final Map<String, String> CONDITION_COLORS = new HashMap<>();
    
    static {
        // Clear/Sunny
        CONDITION_COLORS.put("clear", "#4FC3F7");       // Light blue
        CONDITION_COLORS.put("sunny", "#FFA000");       // Amber
        
        // Cloudy conditions
        CONDITION_COLORS.put("partly cloudy", "#78909C"); // Blue grey
        CONDITION_COLORS.put("mostly cloudy", "#607D8B"); // Darker blue grey
        CONDITION_COLORS.put("cloudy", "#546E7A");      // Even darker blue grey
        CONDITION_COLORS.put("overcast", "#455A64");    // Very dark blue grey
        
        // Rain conditions
        CONDITION_COLORS.put("light rain", "#42A5F5");  // Light blue
        CONDITION_COLORS.put("rain", "#1E88E5");        // Medium blue
        CONDITION_COLORS.put("heavy rain", "#1565C0");  // Dark blue
        CONDITION_COLORS.put("showers", "#1976D2");     // Medium-dark blue
        CONDITION_COLORS.put("drizzle", "#64B5F6");     // Very light blue
        
        // Snow conditions
        CONDITION_COLORS.put("light snow", "#E1F5FE");  // Very light blue
        CONDITION_COLORS.put("snow", "#B3E5FC");        // Light cyan
        CONDITION_COLORS.put("heavy snow", "#81D4FA");  // Cyan
        CONDITION_COLORS.put("blizzard", "#4FC3F7");    // Darker cyan
        
        // Storm conditions
        CONDITION_COLORS.put("thunderstorm", "#5E35B1"); // Deep purple
        CONDITION_COLORS.put("storm", "#512DA8");       // Darker purple
        CONDITION_COLORS.put("lightning", "#673AB7");   // Purple
        
        // Fog/Mist conditions
        CONDITION_COLORS.put("fog", "#B0BEC5");         // Light grey
        CONDITION_COLORS.put("mist", "#CFD8DC");        // Very light grey
        CONDITION_COLORS.put("haze", "#ECEFF1");        // Almost white grey
        
        // Default color if condition not found
        CONDITION_COLORS.put("default", "#3498db");     // Default blue
    }
    
    /**
     * Get the appropriate color for a weather condition and current time
     * 
     * @param condition Weather condition text
     * @return Color as a hex string
     */
    public static String getColorForWeather(String condition) {
        if (condition == null) {
            return CONDITION_COLORS.get("default");
        }
        
        // Normalize condition text
        condition = condition.toLowerCase().trim();
        
        // Find the closest matching condition
        String colorKey = "default";
        for (String key : CONDITION_COLORS.keySet()) {
            if (condition.contains(key)) {
                colorKey = key;
                break;
            }
        }
        
        // Get base color for the condition
        String baseColor = CONDITION_COLORS.get(colorKey);
        
        // Modify color based on time of day
        return blendWithTimeOfDay(baseColor);
    }
    
    /**
     * Blend the base color with a time-of-day tint
     * 
     * @param baseColor Base color as hex string
     * @return Blended color as hex string
     */
    private static String blendWithTimeOfDay(String baseColor) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String timeModifier;
        
        // Determine time of day
        if (hour >= 5 && hour < 10) {
            // Morning (5 AM - 10 AM)
            timeModifier = MORNING_MODIFIER;
        } else if (hour >= 10 && hour < 17) {
            // Day (10 AM - 5 PM)
            timeModifier = DAY_MODIFIER;
        } else if (hour >= 17 && hour < 21) {
            // Evening (5 PM - 9 PM)
            timeModifier = EVENING_MODIFIER;
        } else {
            // Night (9 PM - 5 AM)
            timeModifier = NIGHT_MODIFIER;
        }
        
        // Blend colors (70% base color, 30% time modifier)
        return blendColors(baseColor, timeModifier, 0.7f);
    }
    
    /**
     * Blend two colors with the given ratio
     * 
     * @param color1 First color as hex string
     * @param color2 Second color as hex string
     * @param ratio Blend ratio (0.0 - 1.0) where 1.0 means 100% of color1
     * @return Blended color as hex string
     */
    private static String blendColors(String color1, String color2, float ratio) {
        int c1 = Color.parseColor(color1);
        int c2 = Color.parseColor(color2);
        
        float inverseRatio = 1.0f - ratio;
        
        int r = Math.round(Color.red(c1) * ratio + Color.red(c2) * inverseRatio);
        int g = Math.round(Color.green(c1) * ratio + Color.green(c2) * inverseRatio);
        int b = Math.round(Color.blue(c1) * ratio + Color.blue(c2) * inverseRatio);
        
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
