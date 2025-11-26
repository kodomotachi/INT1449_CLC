package com.example.weatherapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WeatherPageFragment extends Fragment {
    
    private static final String ARG_CITY = "city";
    private City city;
    
    private ImageView ivBackground;
    private TextView tvCurrentTemp;
    private TextView tvWeatherCondition;
    private RecyclerView rvHourlyForecast;

    public static WeatherPageFragment newInstance(City city) {
        WeatherPageFragment fragment = new WeatherPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CITY, city.getName());
        fragment.setArguments(args);
        fragment.city = city;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String cityName = getArguments().getString(ARG_CITY);
            // City object will be passed directly
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupWeatherData();
        setupHourlyForecast();
    }

    private void initializeViews(View view) {
        ivBackground = view.findViewById(R.id.ivBackground);
        tvCurrentTemp = view.findViewById(R.id.tvCurrentTemp);
        tvWeatherCondition = view.findViewById(R.id.tvWeatherCondition);
        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast);
    }

    private void setupWeatherData() {
        // Set background based on weather condition
        ivBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_accent));
        
        // Set sample weather data (in real app, fetch from API based on city)
        tvCurrentTemp.setText("28°");
        tvWeatherCondition.setText("Cloudy  30°/24°");
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
        
        HourlyForecastAdapter adapter = new HourlyForecastAdapter(hourlyForecasts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvHourlyForecast.setLayoutManager(layoutManager);
        rvHourlyForecast.setAdapter(adapter);
    }

    public City getCity() {
        return city;
    }
}

