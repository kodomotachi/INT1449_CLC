package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class WeatherPagerAdapter extends FragmentStateAdapter {

    private List<City> cities;

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

    public void updateCities(List<City> newCities) {
        this.cities = newCities;
        notifyDataSetChanged();
    }
}

