package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.weatherapp.model.City;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new CityDatabaseHelper(this);

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Setup ViewPager
        setupViewPager();

        // Setup click listeners
        setupClickListeners();

        // Set background
        setWeatherBackground("cloudy");
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

        // If no cities, add default
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
