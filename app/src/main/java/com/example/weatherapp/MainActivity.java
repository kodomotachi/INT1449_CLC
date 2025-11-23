package com.example.weatherapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.model.LocationService;
import com.example.weatherapp.model.NominatimCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView ivBackground;
    private TextView tvLocationName;
    private TextView tvCurrentTemp;
    private TextView tvWeatherCondition;
    private TextView tvLocationPermission;
    private RecyclerView rvHourlyForecast;
    private HourlyForecastAdapter hourlyForecastAdapter;
    
    private TextView tvUvValue;
    private TextView tvHumidityValue;
    private TextView tvRealFeelValue;
    private TextView tvWindValue;
    private TextView tvSunsetValue;
    private TextView tvPressureValue;
    
    private ImageButton btnAdd;
    private ImageButton btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        initializeViews();
        
        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Setup UI with sample data
        setupUI();
        
        // Setup RecyclerView
        setupHourlyForecast();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    private void initializeViews() {
        ivBackground = findViewById(R.id.ivBackground);
        tvLocationName = findViewById(R.id.tvLocationName);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        tvLocationPermission = findViewById(R.id.tvLocationPermission);
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        
        tvUvValue = findViewById(R.id.tvUvValue);
        tvHumidityValue = findViewById(R.id.tvHumidityValue);
        tvRealFeelValue = findViewById(R.id.tvRealFeelValue);
        tvWindValue = findViewById(R.id.tvWindValue);
        tvSunsetValue = findViewById(R.id.tvSunsetValue);
        tvPressureValue = findViewById(R.id.tvPressureValue);
        
        btnAdd = findViewById(R.id.btnAdd);
        btnMenu = findViewById(R.id.btnMenu);
    }
    
    private void setupUI() {
        // Set background based on weather condition (cloudy in this example)
        setWeatherBackground("cloudy");
        
        // Set location and weather data
        tvLocationName.setText("Binh Tan");
        tvCurrentTemp.setText("28°");
        tvWeatherCondition.setText("Cloudy  30°/24°");
        
        // Set weather info card values
        tvUvValue.setText(R.string.moderate);
        tvHumidityValue.setText("88%");
        tvRealFeelValue.setText("28°");
        tvWindValue.setText("Force 2");
        tvSunsetValue.setText("17:28");
        tvPressureValue.setText("1007");
    }
    
    private void setWeatherBackground(String condition) {
        // This method sets the background image based on weather condition
        // You can replace this with actual weather backgrounds from the assets folder
        int backgroundResource = R.drawable.ic_launcher_background;
        
        switch (condition.toLowerCase()) {
            case "sunny":
                // backgroundResource = R.drawable.sunny_background;
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
                break;
            case "rainy":
                // backgroundResource = R.drawable.rain_background;
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.text_gray));
                break;
            case "cloudy":
                // backgroundResource = R.drawable.cloud_background;
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
                break;
            case "snowy":
                // backgroundResource = R.drawable.snow_background;
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                break;
            default:
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
                break;
        }
        
        // If you want to use drawable images:
        // ivBackground.setImageResource(backgroundResource);
    }
    
    private void setupHourlyForecast() {
        // Create sample hourly forecast data
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        hourlyForecasts.add(new HourlyForecast("28°", "Now", "Force 2", R.drawable.ic_cloud));
        hourlyForecasts.add(new HourlyForecast("30°", "12:00", "Force 2", R.drawable.ic_cloud));
        hourlyForecasts.add(new HourlyForecast("29°", "13:00", "Force 2", R.drawable.ic_rain));
        hourlyForecasts.add(new HourlyForecast("29°", "14:00", "Force 2", R.drawable.ic_rain));
        hourlyForecasts.add(new HourlyForecast("29°", "15:00", "Force 2", R.drawable.ic_rain));
        hourlyForecasts.add(new HourlyForecast("28°", "16:00", "Force 2", R.drawable.ic_rain));
        hourlyForecasts.add(new HourlyForecast("28°", "17:00", "Force 2", R.drawable.ic_rain));
        hourlyForecasts.add(new HourlyForecast("27°", "18:00", "Force 2", R.drawable.ic_cloud));
        hourlyForecasts.add(new HourlyForecast("27°", "19:00", "Force 2", R.drawable.ic_cloud));
        hourlyForecasts.add(new HourlyForecast("26°", "20:00", "Force 2", R.drawable.ic_cloud));
        
        // Setup RecyclerView
        hourlyForecastAdapter = new HourlyForecastAdapter(hourlyForecasts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvHourlyForecast.setLayoutManager(layoutManager);
        rvHourlyForecast.setAdapter(hourlyForecastAdapter);
    }
    
    private void setupClickListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Add location clicked", Toast.LENGTH_SHORT).show();
                // TODO: Implement add location functionality
            }
        });
        
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Settings Activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        
        tvLocationPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Request location permission", Toast.LENGTH_SHORT).show();
                // TODO: Implement location permission request
            }
        });
    }
}