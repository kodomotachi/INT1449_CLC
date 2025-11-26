package com.example.weatherapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_TEMP_UNIT = "temperature_unit";
    private static final String KEY_WIND_UNIT = "wind_speed_unit";
    private static final String KEY_NIGHT_UPDATE = "night_update_enabled";

    public static final String TEMP_CELSIUS = "celsius";
    public static final String TEMP_FAHRENHEIT = "fahrenheit";
    public static final String WIND_BEAUFORT = "beaufort";
    public static final String WIND_KMH = "kmh";
    public static final String WIND_MS = "ms";

    private SharedPreferences sharedPreferences;
    private TextView tvTemperatureUnit;
    private TextView tvWindSpeedUnit;
    private SwitchCompat switchNightUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initializeViews();
        loadSavedSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        tvTemperatureUnit = findViewById(R.id.tvTemperatureUnit);
        tvWindSpeedUnit = findViewById(R.id.tvWindSpeedUnit);
        switchNightUpdate = findViewById(R.id.switchNightUpdate);
        RelativeLayout layoutTemperatureUnits = findViewById(R.id.layoutTemperatureUnits);
        RelativeLayout layoutWindSpeedUnits = findViewById(R.id.layoutWindSpeedUnits);

        btnBack.setOnClickListener(v -> finish());

        layoutTemperatureUnits.setOnClickListener(v -> showTemperatureUnitDialog());
        layoutWindSpeedUnits.setOnClickListener(v -> showWindSpeedUnitDialog());
    }

    private void loadSavedSettings() {
        // Load temperature unit
        String tempUnit = sharedPreferences.getString(KEY_TEMP_UNIT, TEMP_CELSIUS);
        updateTemperatureUnitDisplay(tempUnit);

        // Load wind speed unit
        String windUnit = sharedPreferences.getString(KEY_WIND_UNIT, WIND_BEAUFORT);
        updateWindSpeedUnitDisplay(windUnit);

        // Load night update setting
        boolean nightUpdateEnabled = sharedPreferences.getBoolean(KEY_NIGHT_UPDATE, false);
        switchNightUpdate.setChecked(nightUpdateEnabled);
    }

    private void setupClickListeners() {
        switchNightUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_NIGHT_UPDATE, isChecked).apply();
            
            if (isChecked) {
                scheduleNightUpdate();
                Toast.makeText(this, "Night update enabled", Toast.LENGTH_SHORT).show();
            } else {
                cancelNightUpdate();
                Toast.makeText(this, "Night update disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTemperatureUnitDialog() {
        String currentUnit = sharedPreferences.getString(KEY_TEMP_UNIT, TEMP_CELSIUS);
        int checkedItem = currentUnit.equals(TEMP_CELSIUS) ? 0 : 1;

        String[] options = {getString(R.string.celsius), getString(R.string.fahrenheit)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_temperature_unit);
        builder.setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
            String selectedUnit = (which == 0) ? TEMP_CELSIUS : TEMP_FAHRENHEIT;
            sharedPreferences.edit().putString(KEY_TEMP_UNIT, selectedUnit).apply();
            updateTemperatureUnitDisplay(selectedUnit);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showWindSpeedUnitDialog() {
        String currentUnit = sharedPreferences.getString(KEY_WIND_UNIT, WIND_BEAUFORT);
        int checkedItem = 0;
        if (currentUnit.equals(WIND_KMH)) checkedItem = 1;
        else if (currentUnit.equals(WIND_MS)) checkedItem = 2;

        String[] options = {
            getString(R.string.beaufort_scale),
            getString(R.string.kilometers_per_hour),
            getString(R.string.meters_per_second)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_wind_speed_unit);
        builder.setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
            String selectedUnit;
            switch (which) {
                case 1:
                    selectedUnit = WIND_KMH;
                    break;
                case 2:
                    selectedUnit = WIND_MS;
                    break;
                default:
                    selectedUnit = WIND_BEAUFORT;
                    break;
            }
            sharedPreferences.edit().putString(KEY_WIND_UNIT, selectedUnit).apply();
            updateWindSpeedUnitDisplay(selectedUnit);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void updateTemperatureUnitDisplay(String unit) {
        if (unit.equals(TEMP_CELSIUS)) {
            tvTemperatureUnit.setText(R.string.celsius);
        } else {
            tvTemperatureUnit.setText(R.string.fahrenheit);
        }
    }

    private void updateWindSpeedUnitDisplay(String unit) {
        switch (unit) {
            case WIND_KMH:
                tvWindSpeedUnit.setText(R.string.kilometers_per_hour);
                break;
            case WIND_MS:
                tvWindSpeedUnit.setText(R.string.meters_per_second);
                break;
            default:
                tvWindSpeedUnit.setText(R.string.beaufort_scale);
                break;
        }
    }

    private void scheduleNightUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WeatherUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set alarm to trigger at 23:00 (11:00 PM)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past 23:00, schedule for next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Schedule repeating alarm
        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
        }
    }

    private void cancelNightUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WeatherUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    // Helper methods to get saved preferences from other activities
    public static String getTemperatureUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TEMP_UNIT, TEMP_CELSIUS);
    }

    public static String getWindSpeedUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_WIND_UNIT, WIND_BEAUFORT);
    }

    public static boolean isNightUpdateEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NIGHT_UPDATE, false);
    }
}

