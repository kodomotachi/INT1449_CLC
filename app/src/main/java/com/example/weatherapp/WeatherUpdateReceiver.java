package com.example.weatherapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class WeatherUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "WeatherUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if night update is enabled
        if (!SettingsActivity.isNightUpdateEnabled(context)) {
            return;
        }

        // Check if current time is between 23:00 and 07:00
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 23 || hour < 7) {
            // Perform weather update
            updateWeatherData(context);
        }
    }

    private void updateWeatherData(Context context) {
        Log.d(TAG, "Updating weather data at night...");
        
        // TODO: Implement actual weather API call here
        // For now, just log the update
        // In a real app, you would:
        // 1. Fetch weather data from API
        // 2. Update local database or SharedPreferences
        // 3. Update notification if app is in background
        
        // Example placeholder
        Toast.makeText(context, "Weather data updated", Toast.LENGTH_SHORT).show();
    }
}

