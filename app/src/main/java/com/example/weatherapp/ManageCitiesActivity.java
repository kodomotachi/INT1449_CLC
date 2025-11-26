package com.example.weatherapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageCitiesActivity extends AppCompatActivity implements CityAdapter.OnCityClickListener {

    private RecyclerView rvCities;
    private EditText etSearchCity;
    private CityAdapter cityAdapter;
    private CityManager cityManager;
    private List<City> cities;

    // Sample cities database for search
    private static final String[][] WORLD_CITIES = {
        {"Singapore", "Singapore"},
        {"Hanoi", "Vietnam"},
        {"Ho Chi Minh City", "Vietnam"},
        {"Da Nang", "Vietnam"},
        {"Bangkok", "Thailand"},
        {"Tokyo", "Japan"},
        {"Seoul", "South Korea"},
        {"Beijing", "China"},
        {"Shanghai", "China"},
        {"Hong Kong", "Hong Kong"},
        {"Kuala Lumpur", "Malaysia"},
        {"Jakarta", "Indonesia"},
        {"Manila", "Philippines"},
        {"New York", "USA"},
        {"Los Angeles", "USA"},
        {"London", "UK"},
        {"Paris", "France"},
        {"Sydney", "Australia"},
        {"Dubai", "UAE"},
        {"Mumbai", "India"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cities);

        cityManager = new CityManager(this);
        
        initializeViews();
        loadCities();
        setupSearch();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        rvCities = findViewById(R.id.rvCities);
        etSearchCity = findViewById(R.id.etSearchCity);

        btnBack.setOnClickListener(v -> finish());

        rvCities.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCities() {
        cities = cityManager.getCities();
        cityAdapter = new CityAdapter(cities, this);
        rvCities.setAdapter(cityAdapter);
    }

    private void setupSearch() {
        etSearchCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(etSearchCity.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.enter_location, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if city already exists
        if (cityManager.cityExists(query)) {
            Toast.makeText(this, R.string.city_already_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        // Search in database
        String[] foundCity = findCity(query);
        
        if (foundCity != null) {
            showAddCityDialog(foundCity[0], foundCity[1]);
        } else {
            Toast.makeText(this, R.string.city_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private String[] findCity(String query) {
        String lowerQuery = query.toLowerCase();
        
        for (String[] city : WORLD_CITIES) {
            if (city[0].toLowerCase().contains(lowerQuery) || 
                city[0].toLowerCase().equals(lowerQuery)) {
                return city;
            }
        }
        return null;
    }

    private void showAddCityDialog(String cityName, String country) {
        new AlertDialog.Builder(this)
            .setTitle("Add City")
            .setMessage("Add " + cityName + ", " + country + "?")
            .setPositiveButton("Add", (dialog, which) -> {
                City newCity = new City(cityName, country, false);
                cityManager.addCity(newCity);
                
                // Reload cities
                cities = cityManager.getCities();
                cityAdapter.updateCities(cities);
                
                // Clear search
                etSearchCity.setText("");
                
                Toast.makeText(this, R.string.city_added, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onCityClick(City city, int position) {
        // Save selected city index and go back to main
        cityManager.saveCurrentCityIndex(position);
        finish();
    }

    @Override
    public void onDeleteClick(City city, int position) {
        if (city.isDefault()) {
            Toast.makeText(this, R.string.default_city, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage("Delete " + city.getName() + "?")
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                if (cityManager.removeCity(position)) {
                    cities = cityManager.getCities();
                    cityAdapter.updateCities(cities);
                    Toast.makeText(this, "City deleted", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}

