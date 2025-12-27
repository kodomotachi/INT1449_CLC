package com.example.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.weatherapp.model.City;
import com.example.weatherapp.model.LocationHelper;
import com.example.weatherapp.model.LocationService;
import com.example.weatherapp.model.NominatimCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView ivBackground;
    private ViewPager2 viewPager;
    private ImageButton btnAdd;
    private ImageButton btnMenu;

    private CityDatabaseHelper dbHelper;
    private List<City> cities;
    private WeatherPagerAdapter pagerAdapter;
    private LocationService locationService;
    
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new CityDatabaseHelper(this);
        locationService = new LocationService();

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Setup ViewPager first (with empty list, will be updated after GPS)
        setupViewPager();

        // Setup click listeners
        setupClickListeners();

        // Set background
        setWeatherBackground("cloudy");

        // Check and request location permission (this will load cities after GPS)
        checkLocationPermission();
    }

    private void initializeViews() {
        ivBackground = findViewById(R.id.ivBackground);
        viewPager = findViewById(R.id.viewPager);
        btnAdd = findViewById(R.id.btnAdd);
        btnMenu = findViewById(R.id.btnMenu);
    }

    private void setupViewPager() {
        cities = new ArrayList<>();
        loadCities();

        pagerAdapter = new WeatherPagerAdapter(this, cities);
        viewPager.setAdapter(pagerAdapter);

        // Optional: Add page transformer for smooth transitions
        viewPager.setPageTransformer((page, position) -> {
            page.setAlpha(1 - Math.abs(position));
        });
    }

    private void loadCities() {
        cities.clear();
        cities.addAll(dbHelper.getAllCities());

        // If no cities, add default (fallback)
        if (cities.isEmpty()) {
            City defaultCity = new City("Binh Tan", "Vietnam", 10.7333, 106.6167);
            defaultCity.setDefault(true);
            long id = dbHelper.addCity(defaultCity);
            defaultCity.setId((int) id);
            cities.add(defaultCity);
        }

        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Check location permission and request if needed
     */
    private void checkLocationPermission() {
        if (LocationHelper.hasLocationPermission(this)) {
            // Permission already granted, get GPS location
            getCurrentLocationAndCreateCity();
        } else {
            // Load cities first (fallback)
            loadCities();
            
            // Request permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show explanation dialog
                new AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("This app needs location permission to show weather for your current location.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        requestLocationPermission();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            } else {
                // Request permission directly
                requestLocationPermission();
            }
        }
    }

    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            PERMISSION_REQUEST_CODE
        );
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get GPS location
                getCurrentLocationAndCreateCity();
            } else {
                // Permission denied
                Log.d(TAG, "Location permission denied");
                // Continue with default city
                loadCities();
            }
        }
    }

    /**
     * Get current GPS location and create/update city
     */
    private void getCurrentLocationAndCreateCity() {
        if (!LocationHelper.isLocationEnabled(this)) {
            Log.d(TAG, "Location services not enabled");
            loadCities();
            return;
        }

        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                Log.d(TAG, "GPS Location received: " + latitude + ", " + longitude);
                
                // Check if city with these coordinates already exists
                City existingCity = dbHelper.getCityByCoordinates(latitude, longitude);
                if (existingCity != null) {
                    // City exists, set it as default
                    dbHelper.setDefaultCity(existingCity.getId());
                    loadCities();
                    return;
                }

                // Reverse geocode to get city name
                locationService.reverseGeocode(MainActivity.this, latitude, longitude, new NominatimCallback() {
                    @Override
                    public void onSuccess(JSONArray result) {
                        try {
                            JSONObject place = result.getJSONObject(0);
                            JSONObject address = place.optJSONObject("address");
                            
                            String cityName = "";
                            String country = "";
                            
                            if (address != null) {
                                // Try to get city name from various fields
                                cityName = address.optString("city", "");
                                if (cityName.isEmpty()) {
                                    cityName = address.optString("town", "");
                                }
                                if (cityName.isEmpty()) {
                                    cityName = address.optString("village", "");
                                }
                                if (cityName.isEmpty()) {
                                    cityName = address.optString("municipality", "");
                                }
                                if (cityName.isEmpty()) {
                                    cityName = address.optString("county", "");
                                }
                                
                                country = address.optString("country", "");
                            }
                            
                            // Fallback to display_name if address not available
                            if (cityName.isEmpty()) {
                                String displayName = place.optString("display_name", "");
                                if (!displayName.isEmpty()) {
                                    String[] parts = displayName.split(",");
                                    if (parts.length > 0) {
                                        cityName = parts[0].trim();
                                    }
                                    if (parts.length > 1) {
                                        country = parts[parts.length - 1].trim();
                                    }
                                }
                            }
                            
                            // Use "Current Location" as fallback
                            if (cityName.isEmpty()) {
                                cityName = "Current Location";
                            }
                            
                            // Create city from GPS location
                            City gpsCity = new City(cityName, country, latitude, longitude);
                            gpsCity.setDefault(true);
                            
                            // Add GPS city to database first
                            long id = dbHelper.addCity(gpsCity);
                            gpsCity.setId((int) id);
                            
                            // Set as default (this will clear other defaults)
                            dbHelper.setDefaultCity((int) id);
                            
                            // Reload cities and update UI
                            runOnUiThread(() -> {
                                loadCities();
                                // Move to first page (GPS location)
                                if (viewPager != null && cities.size() > 0) {
                                    viewPager.setCurrentItem(0, false);
                                }
                            });
                            
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing reverse geocode: " + e.getMessage());
                            // Create city with coordinates only
                            createCityFromCoordinates(latitude, longitude);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Reverse geocode error: " + message);
                        // Create city with coordinates only
                        createCityFromCoordinates(latitude, longitude);
                    }
                });
            }

            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "Location error: " + error);
                // Continue with existing cities
                runOnUiThread(() -> loadCities());
            }
        });
    }

    /**
     * Create city from coordinates when reverse geocoding fails
     */
    private void createCityFromCoordinates(double latitude, double longitude) {
        City gpsCity = new City("Current Location", "", latitude, longitude);
        gpsCity.setDefault(true);
        
        // Add GPS city to database first
        long id = dbHelper.addCity(gpsCity);
        gpsCity.setId((int) id);
        
        // Set as default (this will clear other defaults)
        dbHelper.setDefaultCity((int) id);
        
        // Reload cities and update UI
        runOnUiThread(() -> {
            loadCities();
            if (viewPager != null && cities.size() > 0) {
                viewPager.setCurrentItem(0, false);
            }
        });
    }

    private void setupClickListeners() {
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageCitiesActivity.class);
            startActivity(intent);
        });

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setWeatherBackground(String condition) {
        switch (condition.toLowerCase()) {
            case "sunny":
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
                break;
            case "rainy":
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.text_gray));
                break;
            case "cloudy":
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
                break;
            case "snowy":
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                break;
            default:
                ivBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_accent));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cities when returning from ManageCitiesActivity
        int currentItem = viewPager.getCurrentItem();
        loadCities();

        // Restore position if possible
        if (currentItem < cities.size()) {
            viewPager.setCurrentItem(currentItem, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // ViewPager2 Adapter
    private static class WeatherPagerAdapter extends FragmentStateAdapter {
        private final List<City> cities;

        public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<City> cities) {
            super(fragmentActivity);
            this.cities = cities;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return WeatherPageFragment.newInstance(cities.get(position));
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        @Override
        public long getItemId(int position) {
            return cities.get(position).getId();
        }

        @Override
        public boolean containsItem(long itemId) {
            for (City city : cities) {
                if (city.getId() == itemId) {
                    return true;
                }
            }
            return false;
        }
    }
}
