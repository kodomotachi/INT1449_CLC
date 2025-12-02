package com.example.weatherapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageCitiesActivity extends AppCompatActivity {

    private RecyclerView rvCities;
    private RecyclerView rvSearchResults;
    private EditText etSearchCity;
    private CityDatabaseHelper dbHelper;
    private CitiesAdapter citiesAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    private List<City> cities;
    private List<City> searchResults;

    // Sample world cities database
    private Map<String, City> worldCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cities);

        dbHelper = new CityDatabaseHelper(this);
        initializeWorldCities();
        initializeViews();
        loadCities();
        setupSearch();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        rvCities = findViewById(R.id.rvCities);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        etSearchCity = findViewById(R.id.etSearchCity);

        btnBack.setOnClickListener(v -> finish());

        cities = new ArrayList<>();
        citiesAdapter = new CitiesAdapter(cities);
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(citiesAdapter);

        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter(searchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(searchResultsAdapter);
    }

    private void initializeWorldCities() {
        worldCities = new HashMap<>();
        
        // Vietnam
        worldCities.put("Binh Tan", new City("Binh Tan", "Vietnam", 10.7333, 106.6167));
        worldCities.put("Hanoi", new City("Hanoi", "Vietnam", 21.0285, 105.8542));
        worldCities.put("Ho Chi Minh City", new City("Ho Chi Minh City", "Vietnam", 10.8231, 106.6297));
        worldCities.put("Da Nang", new City("Da Nang", "Vietnam", 16.0544, 108.2022));
        worldCities.put("Hue", new City("Hue", "Vietnam", 16.4637, 107.5909));
        
        // Southeast Asia
        worldCities.put("Singapore", new City("Singapore", "Singapore", 1.3521, 103.8198));
        worldCities.put("Bangkok", new City("Bangkok", "Thailand", 13.7563, 100.5018));
        worldCities.put("Kuala Lumpur", new City("Kuala Lumpur", "Malaysia", 3.1390, 101.6869));
        worldCities.put("Jakarta", new City("Jakarta", "Indonesia", -6.2088, 106.8456));
        worldCities.put("Manila", new City("Manila", "Philippines", 14.5995, 120.9842));
        
        // East Asia
        worldCities.put("Tokyo", new City("Tokyo", "Japan", 35.6762, 139.6503));
        worldCities.put("Seoul", new City("Seoul", "South Korea", 37.5665, 126.9780));
        worldCities.put("Beijing", new City("Beijing", "China", 39.9042, 116.4074));
        worldCities.put("Shanghai", new City("Shanghai", "China", 31.2304, 121.4737));
        worldCities.put("Hong Kong", new City("Hong Kong", "Hong Kong", 22.3193, 114.1694));
        worldCities.put("Taipei", new City("Taipei", "Taiwan", 25.0330, 121.5654));
        
        // Australia & Oceania
        worldCities.put("Sydney", new City("Sydney", "Australia", -33.8688, 151.2093));
        worldCities.put("Melbourne", new City("Melbourne", "Australia", -37.8136, 144.9631));
        worldCities.put("Auckland", new City("Auckland", "New Zealand", -36.8485, 174.7633));
        
        // Europe
        worldCities.put("London", new City("London", "United Kingdom", 51.5074, -0.1278));
        worldCities.put("Paris", new City("Paris", "France", 48.8566, 2.3522));
        worldCities.put("Berlin", new City("Berlin", "Germany", 52.5200, 13.4050));
        worldCities.put("Rome", new City("Rome", "Italy", 41.9028, 12.4964));
        worldCities.put("Madrid", new City("Madrid", "Spain", 40.4168, -3.7038));
        worldCities.put("Moscow", new City("Moscow", "Russia", 55.7558, 37.6173));
        
        // North America
        worldCities.put("New York", new City("New York", "United States", 40.7128, -74.0060));
        worldCities.put("Los Angeles", new City("Los Angeles", "United States", 34.0522, -118.2437));
        worldCities.put("Chicago", new City("Chicago", "United States", 41.8781, -87.6298));
        worldCities.put("Toronto", new City("Toronto", "Canada", 43.6532, -79.3832));
        worldCities.put("Vancouver", new City("Vancouver", "Canada", 49.2827, -123.1207));
        
        // South America
        worldCities.put("São Paulo", new City("São Paulo", "Brazil", -23.5505, -46.6333));
        worldCities.put("Buenos Aires", new City("Buenos Aires", "Argentina", -34.6037, -58.3816));
        worldCities.put("Rio de Janeiro", new City("Rio de Janeiro", "Brazil", -22.9068, -43.1729));
        
        // Middle East
        worldCities.put("Dubai", new City("Dubai", "UAE", 25.2048, 55.2708));
        worldCities.put("Tel Aviv", new City("Tel Aviv", "Israel", 32.0853, 34.7818));
        worldCities.put("Istanbul", new City("Istanbul", "Turkey", 41.0082, 28.9784));
    }

    private void setupSearch() {
        etSearchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
                // testing
                getCityName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    protected void getCityName(String query) {

    }

    private void performSearch(String query) {
        searchResults.clear();
        
        if (query.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            searchResultsAdapter.notifyDataSetChanged();
            return;
        }

        String lowerQuery = query.toLowerCase();
        for (Map.Entry<String, City> entry : worldCities.entrySet()) {
            City city = entry.getValue();
            if (city.getName().toLowerCase().contains(lowerQuery) || 
                city.getCountry().toLowerCase().contains(lowerQuery)) {
                searchResults.add(city);
            }
        }

        if (searchResults.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
        }
        searchResultsAdapter.notifyDataSetChanged();
    }

    private void loadCities() {
        cities.clear();
        cities.addAll(dbHelper.getAllCities());
        
        // Add sample weather data
        for (City city : cities) {
            city.setTemperature("28");
            city.setWeatherCondition("Cloudy");
            city.setHighTemp("30");
            city.setLowTemp("24");
        }
        
        citiesAdapter.notifyDataSetChanged();
    }

    // Cities Adapter
    private class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.CityViewHolder> {
        private List<City> cities;

        public CitiesAdapter(List<City> cities) {
            this.cities = cities;
        }

        @NonNull
        @Override
        public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_city, parent, false);
            return new CityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
            City city = cities.get(position);
            holder.tvCityName.setText(city.getName());
            holder.tvCityWeather.setText(city.getWeatherCondition() + "  " + 
                    city.getHighTemp() + "° / " + city.getLowTemp() + "°");
            holder.tvCityTemp.setText(city.getTemperature() + "°");

            if (city.isDefault()) {
                holder.ivLocationPin.setVisibility(View.VISIBLE);
                holder.btnDeleteCity.setVisibility(View.GONE);
            } else {
                holder.ivLocationPin.setVisibility(View.GONE);
                holder.btnDeleteCity.setVisibility(View.VISIBLE);
            }

            holder.btnDeleteCity.setOnClickListener(v -> {
                if (dbHelper.getCityCount() <= 1) {
                    Toast.makeText(ManageCitiesActivity.this, 
                            R.string.cannot_remove_last_city, Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.deleteCity(city.getId());
                loadCities();
                Toast.makeText(ManageCitiesActivity.this, 
                        R.string.city_removed, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        class CityViewHolder extends RecyclerView.ViewHolder {
            TextView tvCityName, tvCityWeather, tvCityTemp;
            ImageView ivLocationPin;
            ImageButton btnDeleteCity;

            public CityViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCityName = itemView.findViewById(R.id.tvCityName);
                tvCityWeather = itemView.findViewById(R.id.tvCityWeather);
                tvCityTemp = itemView.findViewById(R.id.tvCityTemp);
                ivLocationPin = itemView.findViewById(R.id.ivLocationPin);
                btnDeleteCity = itemView.findViewById(R.id.btnDeleteCity);
            }
        }
    }

    // Search Results Adapter
    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchViewHolder> {
        private List<City> results;

        public SearchResultsAdapter(List<City> results) {
            this.results = results;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_result, parent, false);
            return new SearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            City city = results.get(position);
            holder.tvSearchResultName.setText(city.getName());
            holder.tvSearchResultCountry.setText(city.getCountry());

            holder.itemView.setOnClickListener(v -> {
                if (dbHelper.cityExists(city.getName())) {
                    Toast.makeText(ManageCitiesActivity.this, 
                            R.string.city_already_added, Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.addCity(city);
                loadCities();
                etSearchCity.setText("");
                rvSearchResults.setVisibility(View.GONE);
                Toast.makeText(ManageCitiesActivity.this, 
                        R.string.city_added, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        class SearchViewHolder extends RecyclerView.ViewHolder {
            TextView tvSearchResultName, tvSearchResultCountry;

            public SearchViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSearchResultName = itemView.findViewById(R.id.tvSearchResultName);
                tvSearchResultCountry = itemView.findViewById(R.id.tvSearchResultCountry);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}

