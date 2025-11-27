package com.example.weatherapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WeatherPageFragment extends Fragment {

    private static final String ARG_CITY = "city";
    
    private City city;
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

    public static WeatherPageFragment newInstance(City city) {
        WeatherPageFragment fragment = new WeatherPageFragment();
        Bundle args = new Bundle();
        args.putInt("city_id", city.getId());
        args.putString("city_name", city.getName());
        args.putString("city_country", city.getCountry());
        args.putDouble("city_lat", city.getLatitude());
        args.putDouble("city_lon", city.getLongitude());
        args.putBoolean("city_default", city.isDefault());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = new City(
                getArguments().getInt("city_id"),
                getArguments().getString("city_name"),
                getArguments().getString("city_country"),
                getArguments().getDouble("city_lat"),
                getArguments().getDouble("city_lon"),
                getArguments().getBoolean("city_default")
            );
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
        setupUI();
        setupHourlyForecast();
    }

    private void initializeViews(View view) {
        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvCurrentTemp = view.findViewById(R.id.tvCurrentTemp);
        tvWeatherCondition = view.findViewById(R.id.tvWeatherCondition);
        tvLocationPermission = view.findViewById(R.id.tvLocationPermission);
        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast);
        
        tvUvValue = view.findViewById(R.id.tvUvValue);
        tvHumidityValue = view.findViewById(R.id.tvHumidityValue);
        tvRealFeelValue = view.findViewById(R.id.tvRealFeelValue);
        tvWindValue = view.findViewById(R.id.tvWindValue);
        tvSunsetValue = view.findViewById(R.id.tvSunsetValue);
        tvPressureValue = view.findViewById(R.id.tvPressureValue);
    }

    private void setupUI() {
        if (city != null) {
            tvLocationName.setText(city.getName());
        }
        
        // Sample data - replace with actual weather data
        tvCurrentTemp.setText("28°");
        tvWeatherCondition.setText("Cloudy  30°/24°");
        
        tvUvValue.setText(R.string.moderate);
        tvHumidityValue.setText("88%");
        tvRealFeelValue.setText("28°");
        tvWindValue.setText("Force 2");
        tvSunsetValue.setText("17:28");
        tvPressureValue.setText("1007");
    }

    private void setupHourlyForecast() {
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

        hourlyForecastAdapter = new HourlyForecastAdapter(hourlyForecasts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvHourlyForecast.setLayoutManager(layoutManager);
        rvHourlyForecast.setAdapter(hourlyForecastAdapter);
    }
}

