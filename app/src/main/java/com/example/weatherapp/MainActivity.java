package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView ivBackground;
    private TextView tvLocationName;
    private TextView tvLocationPermission;
    private ImageButton btnAdd;
    private ImageButton btnMenu;
    
    private ViewPager2 viewPagerWeather;
    private WeatherPagerAdapter pagerAdapter;
    private CityManager cityManager;
    private List<City> cities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        cityManager = new CityManager(this);
        
        // Initialize views
        initializeViews();
        
        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Setup UI
        setupUI();
        
        // Setup ViewPager
        setupViewPager();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cities when returning from ManageCitiesActivity
        refreshCities();
    }
    
    private void initializeViews() {
        ivBackground = findViewById(R.id.ivBackground);
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationPermission = findViewById(R.id.tvLocationPermission);
        btnAdd = findViewById(R.id.btnAdd);
        btnMenu = findViewById(R.id.btnMenu);
        viewPagerWeather = findViewById(R.id.viewPagerWeather);
    }
    
    private void setupUI() {
        // Set background based on weather condition
        ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
    }
    
    private void setupViewPager() {
        cities = cityManager.getCities();
        pagerAdapter = new WeatherPagerAdapter(this, cities);
        viewPagerWeather.setAdapter(pagerAdapter);
        
        // Set to saved city index
        int savedIndex = cityManager.getCurrentCityIndex();
        if (savedIndex < cities.size()) {
            viewPagerWeather.setCurrentItem(savedIndex, false);
            updateLocationName(cities.get(savedIndex));
        }
        
        // Listen for page changes
        viewPagerWeather.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position < cities.size()) {
                    City city = cities.get(position);
                    updateLocationName(city);
                    cityManager.saveCurrentCityIndex(position);
                }
            }
        });
    }
    
    private void updateLocationName(City city) {
        tvLocationName.setText(city.getName());
    }
    
    private void refreshCities() {
        cities = cityManager.getCities();
        pagerAdapter.updateCities(cities);
        
        // Update current page if needed
        int savedIndex = cityManager.getCurrentCityIndex();
        if (savedIndex < cities.size()) {
            viewPagerWeather.setCurrentItem(savedIndex, false);
            updateLocationName(cities.get(savedIndex));
        }
    }
    
    private void setupClickListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Manage Cities Activity
                Intent intent = new Intent(MainActivity.this, ManageCitiesActivity.class);
                startActivity(intent);
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
