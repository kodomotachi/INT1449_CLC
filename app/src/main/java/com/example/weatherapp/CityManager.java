package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CityManager {
    private static final String PREFS_NAME = "CityPrefs";
    private static final String KEY_CITIES = "cities";
    private static final String KEY_CURRENT_INDEX = "current_city_index";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public CityManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Get all saved cities
     */
    public List<City> getCities() {
        String json = prefs.getString(KEY_CITIES, null);
        if (json == null) {
            return getDefaultCities();
        }
        
        Type type = new TypeToken<List<City>>(){}.getType();
        List<City> cities = gson.fromJson(json, type);
        
        if (cities == null || cities.isEmpty()) {
            return getDefaultCities();
        }
        
        return cities;
    }

    /**
     * Save cities to storage
     */
    public void saveCities(List<City> cities) {
        String json = gson.toJson(cities);
        prefs.edit().putString(KEY_CITIES, json).apply();
    }

    /**
     * Add a new city
     */
    public void addCity(City city) {
        List<City> cities = getCities();
        cities.add(city);
        saveCities(cities);
    }

    /**
     * Remove a city (only if not default)
     */
    public boolean removeCity(int position) {
        List<City> cities = getCities();
        if (position >= 0 && position < cities.size()) {
            City city = cities.get(position);
            if (!city.isDefault()) {
                cities.remove(position);
                saveCities(cities);
                return true;
            }
        }
        return false;
    }

    /**
     * Get default cities list
     */
    private List<City> getDefaultCities() {
        List<City> cities = new ArrayList<>();
        cities.add(new City("Binh Tan", "Vietnam", true, 10.7309, 106.6199));
        return cities;
    }

    /**
     * Get current city index (for ViewPager)
     */
    public int getCurrentCityIndex() {
        return prefs.getInt(KEY_CURRENT_INDEX, 0);
    }

    /**
     * Save current city index
     */
    public void saveCurrentCityIndex(int index) {
        prefs.edit().putInt(KEY_CURRENT_INDEX, index).apply();
    }

    /**
     * Check if city already exists
     */
    public boolean cityExists(String cityName) {
        List<City> cities = getCities();
        for (City city : cities) {
            if (city.getName().equalsIgnoreCase(cityName)) {
                return true;
            }
        }
        return false;
    }
}

