package com.example.weatherapp;

import android.content.Context;

public class UnitConverter {

    /**
     * Convert temperature from Celsius to the user's preferred unit
     */
    public static String formatTemperature(Context context, double celsius) {
        String unit = SettingsActivity.getTemperatureUnit(context);
        
        if (unit.equals(SettingsActivity.TEMP_FAHRENHEIT)) {
            double fahrenheit = celsiusToFahrenheit(celsius);
            return String.format("%.0f°F", fahrenheit);
        } else {
            return String.format("%.0f°C", celsius);
        }
    }

    /**
     * Convert Celsius to Fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    /**
     * Convert Fahrenheit to Celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    /**
     * Format wind speed based on user's preferred unit
     * @param context Application context
     * @param speedKmh Wind speed in km/h
     * @return Formatted wind speed string
     */
    public static String formatWindSpeed(Context context, double speedKmh) {
        String unit = SettingsActivity.getWindSpeedUnit(context);
        
        switch (unit) {
            case SettingsActivity.WIND_BEAUFORT:
                int beaufort = kmhToBeaufort(speedKmh);
                return "Force " + beaufort;
            
            case SettingsActivity.WIND_MS:
                double ms = kmhToMs(speedKmh);
                return String.format("%.1f m/s", ms);
            
            case SettingsActivity.WIND_KMH:
            default:
                return String.format("%.0f km/h", speedKmh);
        }
    }

    /**
     * Convert km/h to Beaufort scale (0-12)
     */
    public static int kmhToBeaufort(double kmh) {
        if (kmh < 1) return 0;
        if (kmh < 6) return 1;
        if (kmh < 12) return 2;
        if (kmh < 20) return 3;
        if (kmh < 29) return 4;
        if (kmh < 39) return 5;
        if (kmh < 50) return 6;
        if (kmh < 62) return 7;
        if (kmh < 75) return 8;
        if (kmh < 89) return 9;
        if (kmh < 103) return 10;
        if (kmh < 118) return 11;
        return 12;
    }

    /**
     * Convert km/h to m/s
     */
    public static double kmhToMs(double kmh) {
        return kmh / 3.6;
    }

    /**
     * Convert m/s to km/h
     */
    public static double msToKmh(double ms) {
        return ms * 3.6;
    }

    /**
     * Get temperature unit symbol
     */
    public static String getTemperatureUnitSymbol(Context context) {
        String unit = SettingsActivity.getTemperatureUnit(context);
        return unit.equals(SettingsActivity.TEMP_FAHRENHEIT) ? "°F" : "°C";
    }

    /**
     * Get wind speed unit symbol
     */
    public static String getWindSpeedUnitSymbol(Context context) {
        String unit = SettingsActivity.getWindSpeedUnit(context);
        
        switch (unit) {
            case SettingsActivity.WIND_BEAUFORT:
                return "Beaufort";
            case SettingsActivity.WIND_MS:
                return "m/s";
            case SettingsActivity.WIND_KMH:
            default:
                return "km/h";
        }
    }
}

