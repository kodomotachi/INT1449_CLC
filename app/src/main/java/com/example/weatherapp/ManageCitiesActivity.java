package com.example.weatherapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.model.City;
import com.example.weatherapp.model.LocationService;
import com.example.weatherapp.model.NominatimCallback;
import com.example.weatherapp.model.WeatherDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageCitiesActivity extends AppCompatActivity {

    private RecyclerView rvCities;
    private RecyclerView rvSearchResults;
    private EditText etSearchCity;
    private RelativeLayout layoutDeleteButton;
    private LinearLayout btnDeleteSelected;

    private CityDatabaseHelper dbHelper;
    private CitiesAdapter citiesAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    private List<City> cities;
    private List<City> searchResults;
    private ItemTouchHelper itemTouchHelper;

    // Edit mode state
    private boolean isEditMode = false;
    private List<Integer> selectedCityIds = new ArrayList<>();

    // Constants
    private static final String TAG = "ManageCitiesActivity";
    private static final int MAX_SUGGESTIONS = 5;

    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cities);

        dbHelper = new CityDatabaseHelper(this);
        locationService = new LocationService();
        initializeViews();
        loadCities();
        setupSearch();
        setupItemTouchHelper();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        rvCities = findViewById(R.id.rvCities);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        etSearchCity = findViewById(R.id.etSearchCity);
        layoutDeleteButton = findViewById(R.id.layoutDeleteButton);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        btnBack.setOnClickListener(v -> {
            if (isEditMode) {
                exitEditMode();
            } else {
                finish();
            }
        });

        cities = new ArrayList<>();
        citiesAdapter = new CitiesAdapter(cities);
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(citiesAdapter);

        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter(searchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(searchResultsAdapter);

        btnDeleteSelected.setOnClickListener(v -> deleteSelectedCities());
    }

    private void setupSearch() {
        etSearchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    // Clear suggestions when query is empty
                    searchResults.clear();
                    rvSearchResults.setVisibility(View.GONE);
                    searchResultsAdapter.notifyDataSetChanged();
                    return;
                }

                // Send query to backend for suggestions
                onSearchQueryChanged(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Called when search query changes.
     * Override this method or call from backend to fetch suggestions.
     * 
     * @param query The search query string
     */
    // protected void onSearchQueryChanged(String query) {
    // // TODO: Backend should implement this to fetch city suggestions
    // // Example: Call your API with the query and then call
    // onSuggestionsReceived() with results
    // Log.d(TAG, "Search query: " + query);
    // }

    // @Override
    protected void onSearchQueryChanged(String query) {
        // Log để kiểm tra query
        Log.d(TAG, "Search query: " + query);

        // Chỉ tìm kiếm nếu người dùng gõ ít nhất 2 ký tự
        if (query.length() >= 2) {
            Log.d(TAG, "Calling LocationService.fetchSuggestion for: " + query);
            locationService.fetchSuggestion(this, query, new NominatimCallback() {
                @Override
                public void onSuccess(JSONArray result) {
                    Log.d(TAG, "LocationService returned " + result.length() + " results");
                    // Parse Nominatim response và convert sang format cho onSuggestionsReceived
                    parseNominatimResponse(result);
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error fetching suggestions: " + message);
                    runOnUiThread(() -> {
                        searchResults.clear();
                        rvSearchResults.setVisibility(View.GONE);
                        searchResultsAdapter.notifyDataSetChanged();
                    });
                }
            });
        } else {
            // Clear suggestions when query is too short
            searchResults.clear();
            rvSearchResults.setVisibility(View.GONE);
            searchResultsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Parse Nominatim API response and convert to City suggestions format
     * Nominatim API returns places with address object containing various location
     * types
     */
    private void parseNominatimResponse(JSONArray nominatimResults) {
        try {
            Log.d(TAG, "Parsing " + nominatimResults.length() + " Nominatim results");
            JSONArray convertedResults = new JSONArray();

            int count = Math.min(nominatimResults.length(), MAX_SUGGESTIONS);
            Log.d(TAG, "Processing " + count + " results (max: " + MAX_SUGGESTIONS + ")");

            for (int i = 0; i < count; i++) {
                JSONObject place = nominatimResults.getJSONObject(i);

                // Extract coordinates
                String latStr = place.optString("lat", "0");
                String lonStr = place.optString("lon", "0");

                // Validate coordinates
                if (latStr.equals("0") || lonStr.equals("0")) {
                    Log.w(TAG, "Skipping place " + i + " - invalid coordinates");
                    continue;
                }

                double lat, lon;
                try {
                    lat = Double.parseDouble(latStr);
                    lon = Double.parseDouble(lonStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
                    continue;
                }

                // Extract city name and country from address object
                String cityName = "";
                String country = "";

                if (place.has("address")) {
                    try {
                        JSONObject address = place.getJSONObject("address");

                        // Try to get city name from various possible fields in Nominatim response
                        // Priority: city > town > village > municipality > county > state
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
                        if (cityName.isEmpty()) {
                            cityName = address.optString("state", "");
                        }

                        // Get country
                        country = address.optString("country", "");

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing address object: " + e.getMessage());
                    }
                }

                // Fallback: parse from display_name if address is not available or city name is
                // empty
                if (cityName.isEmpty()) {
                    String displayName = place.optString("display_name", "");
                    if (!displayName.isEmpty()) {
                        // Parse display_name format: "City, State, Country" or "City, Country"
                        String[] parts = displayName.split(",");
                        if (parts.length > 0) {
                            cityName = parts[0].trim();
                        }
                        // Get country from the last part
                        if (parts.length > 1) {
                            country = parts[parts.length - 1].trim();
                        }
                    }
                }

                // Use place name as fallback if still empty
                if (cityName.isEmpty()) {
                    cityName = place.optString("name", "");
                }

                // Only add if we have valid city name and coordinates
                if (!cityName.isEmpty()) {
                    JSONObject cityJson = new JSONObject();
                    cityJson.put("name", cityName);
                    cityJson.put("country", country);
                    cityJson.put("lat", lat);
                    cityJson.put("lon", lon);

                    convertedResults.put(cityJson);
                    Log.d(TAG, "Added suggestion " + (convertedResults.length()) + ": " + cityName + ", " + country);
                } else {
                    Log.w(TAG, "Skipping place " + i + " - no city name found");
                }
            }

            Log.d(TAG, "Converted " + convertedResults.length() + " suggestions");

            // Call onSuggestionsReceived with converted format
            if (convertedResults.length() > 0) {
                onSuggestionsReceived(convertedResults);
            } else {
                // No valid suggestions found
                Log.w(TAG, "No valid suggestions found after parsing");
                runOnUiThread(() -> {
                    searchResults.clear();
                    rvSearchResults.setVisibility(View.GONE);
                    searchResultsAdapter.notifyDataSetChanged();
                });
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Nominatim response: " + e.getMessage(), e);
            runOnUiThread(() -> {
                searchResults.clear();
                rvSearchResults.setVisibility(View.GONE);
                searchResultsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Receives city suggestions from backend.
     * Call this method from your backend/API response handler.
     * 
     * @param suggestions JSONArray of city suggestions (maximum 5 will be
     *                    displayed)
     * 
     *                    Expected JSON format:
     *                    [
     *                    {"name": "Singapore", "country": "Singapore", "lat":
     *                    1.3521, "lon": 103.8198},
     *                    {"name": "Sydney", "country": "Australia", "lat":
     *                    -33.8688, "lon": 151.2093},
     *                    ...
     *                    ]
     */
    public void onSuggestionsReceived(JSONArray suggestions) {
        Log.d(TAG, "onSuggestionsReceived called with " + (suggestions != null ? suggestions.length() : 0)
                + " suggestions");
        runOnUiThread(() -> {
            searchResults.clear();

            if (suggestions == null || suggestions.length() == 0) {
                Log.d(TAG, "No suggestions to display");
                rvSearchResults.setVisibility(View.GONE);
                searchResultsAdapter.notifyDataSetChanged();
                return;
            }

            // Parse up to MAX_SUGGESTIONS (5) items
            int count = Math.min(suggestions.length(), MAX_SUGGESTIONS);
            Log.d(TAG, "Adding " + count + " suggestions to list");

            for (int i = 0; i < count; i++) {
                try {
                    JSONObject cityJson = suggestions.getJSONObject(i);

                    String name = cityJson.optString("name", "");
                    String country = cityJson.optString("country", "");
                    double lat = cityJson.optDouble("lat", 0.0);
                    double lon = cityJson.optDouble("lon", 0.0);

                    if (!name.isEmpty()) {
                        City city = new City(name, country, lat, lon);
                        searchResults.add(city);
                        Log.d(TAG, "Added city to searchResults: " + name);
                    } else {
                        Log.w(TAG, "Skipping city at index " + i + " - empty name");
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing city at index " + i + ": " + e.getMessage());
                }
            }

            Log.d(TAG, "Total searchResults size: " + searchResults.size());

            if (searchResults.isEmpty()) {
                Log.d(TAG, "searchResults is empty, hiding RecyclerView");
                rvSearchResults.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Showing RecyclerView with " + searchResults.size() + " items");
                rvSearchResults.setVisibility(View.VISIBLE);
            }
            searchResultsAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Convenience method to receive suggestions as a JSON string.
     * 
     * @param jsonString JSON array string of city suggestions
     */
    public void onSuggestionsReceived(String jsonString) {
        try {
            JSONArray suggestions = new JSONArray(jsonString);
            onSuggestionsReceived(suggestions);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON string: " + e.getMessage());
            runOnUiThread(() -> {
                searchResults.clear();
                rvSearchResults.setVisibility(View.GONE);
                searchResultsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Clears all search suggestions.
     */
    public void clearSuggestions() {
        runOnUiThread(() -> {
            searchResults.clear();
            rvSearchResults.setVisibility(View.GONE);
            searchResultsAdapter.notifyDataSetChanged();
        });
    }

    private void loadCities() {
        cities.clear();
        cities.addAll(dbHelper.getAllCities());

        // Fetch weather data for all cities from API
        fetchWeatherDataForAllCities();

        citiesAdapter.notifyDataSetChanged();
    }

    /**
     * Fetch weather data from API for all cities in the list
     */
    private void fetchWeatherDataForAllCities() {
        for (City city : cities) {
            fetchWeatherDataForCity(city);
        }
    }

    /**
     * Fetch weather data for a single city and update its display
     */
    private void fetchWeatherDataForCity(City city) {
        // Store city ID to find it later
        final int cityId = city.getId();

        WeatherDataAPI.getDataByCoordinates(
                this,
                city.getLatitude(),
                city.getLongitude(),
                new WeatherDataAPI.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject weatherJson = new JSONObject(response);
                            JSONObject current = weatherJson.getJSONObject("current");
                            double temp = current.getDouble("temperature_2m");
                            int weatherCode = current.optInt("weather_code", 0);

                            // Get daily forecast for high/low temps
                            double tempMax = temp;
                            double tempMin = temp;
                            if (weatherJson.has("daily")) {
                                JSONObject daily = weatherJson.getJSONObject("daily");
                                if (daily.has("temperature_2m_max")) {
                                    JSONArray tempMaxArray = daily.getJSONArray("temperature_2m_max");
                                    if (tempMaxArray.length() > 0) {
                                        tempMax = tempMaxArray.getDouble(0);
                                    }
                                }
                                if (daily.has("temperature_2m_min")) {
                                    JSONArray tempMinArray = daily.getJSONArray("temperature_2m_min");
                                    if (tempMinArray.length() > 0) {
                                        tempMin = tempMinArray.getDouble(0);
                                    }
                                }
                            }

                            String weatherCondition = convertWeatherCodeToCondition(weatherCode);

                            // Create final copies for use in lambda
                            final double finalTemp = temp;
                            final double finalTempMax = tempMax;
                            final double finalTempMin = tempMin;
                            final String finalWeatherCondition = weatherCondition;

                            // Update city with weather data
                            runOnUiThread(() -> {
                                // Find city by ID to ensure we update the correct one
                                City cityToUpdate = getCityById(cityId);
                                if (cityToUpdate != null) {
                                    cityToUpdate.setTemperature(String.valueOf((int) Math.round(finalTemp)));
                                    cityToUpdate.setWeatherCondition(finalWeatherCondition);
                                    cityToUpdate.setHighTemp(String.valueOf((int) Math.round(finalTempMax)));
                                    cityToUpdate.setLowTemp(String.valueOf((int) Math.round(finalTempMin)));

                                    // Update the adapter to reflect changes
                                    int position = cities.indexOf(cityToUpdate);
                                    if (position >= 0) {
                                        citiesAdapter.notifyItemChanged(position);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing weather data for " + city.getName() + ": " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error fetching weather data for " + city.getName() + ": " + error);
                    }
                });
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Don't allow moving default city or moving to default city position
                if (cities.get(fromPosition).isDefault() || cities.get(toPosition).isDefault()) {
                    return false;
                }

                // Swap items in the list
                Collections.swap(cities, fromPosition, toPosition);

                // Notify adapter of the move for real-time update
                citiesAdapter.notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // We'll handle drag manually via touch listener
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Optional: Save the new order to database here if needed
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvCities);
    }

    private void enterEditMode() {
        isEditMode = true;
        selectedCityIds.clear();
        layoutDeleteButton.setVisibility(View.VISIBLE);
        etSearchCity.setEnabled(false);
        citiesAdapter.notifyDataSetChanged();
    }

    private void exitEditMode() {
        isEditMode = false;
        selectedCityIds.clear();
        layoutDeleteButton.setVisibility(View.GONE);
        etSearchCity.setEnabled(true);
        citiesAdapter.notifyDataSetChanged();
    }

    private void deleteSelectedCities() {
        if (selectedCityIds.isEmpty()) {
            Toast.makeText(this, "No cities selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if trying to delete default city
        for (int cityId : selectedCityIds) {
            City city = getCityById(cityId);
            if (city != null && city.isDefault()) {
                Toast.makeText(this, R.string.cannot_remove_last_city, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Check if deleting all cities
        if (selectedCityIds.size() >= cities.size()) {
            Toast.makeText(this, R.string.cannot_remove_last_city, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Cities")
                .setMessage("Delete " + selectedCityIds.size() + " selected city(cities)?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (int cityId : selectedCityIds) {
                        dbHelper.deleteCity(cityId);
                    }
                    selectedCityIds.clear();
                    loadCities();
                    exitEditMode();
                    Toast.makeText(this, "Cities deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private City getCityById(int id) {
        for (City city : cities) {
            if (city.getId() == id) {
                return city;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            exitEditMode();
        } else {
            super.onBackPressed();
        }
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

            // Show current temperature instead of high/low range
            String currentTemp = city.getTemperature();
            if (currentTemp != null && !currentTemp.isEmpty()) {
                holder.tvCityWeather.setText(city.getWeatherCondition() + "  " + currentTemp + "°");
            } else {
                holder.tvCityWeather.setText(city.getWeatherCondition() != null ? city.getWeatherCondition() : "");
            }

            holder.tvCityTemp.setText(city.getTemperature() != null && !city.getTemperature().isEmpty()
                    ? city.getTemperature() + "°"
                    : "--°");

            // Show location pin for default city
            if (city.isDefault()) {
                holder.ivLocationPin.setVisibility(View.VISIBLE);
            } else {
                holder.ivLocationPin.setVisibility(View.GONE);
            }

            if (isEditMode) {
                // Edit mode: Show checkbox on the right and drag handle on the left
                holder.cbSelectCity.setVisibility(View.VISIBLE);

                if (city.isDefault()) {
                    // Default city: No drag handle, disabled checkbox
                    holder.ivDragHandle.setVisibility(View.GONE);
                    holder.cbSelectCity.setEnabled(false);
                    holder.cbSelectCity.setAlpha(0.3f);
                } else {
                    // Non-default cities: Show drag handle, enabled checkbox
                    holder.ivDragHandle.setVisibility(View.VISIBLE);
                    holder.ivDragHandle.setAlpha(1.0f);
                    holder.cbSelectCity.setEnabled(true);
                    holder.cbSelectCity.setAlpha(1.0f);
                }

                holder.cbSelectCity.setChecked(selectedCityIds.contains(city.getId()));
                holder.cbSelectCity.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedCityIds.contains(city.getId())) {
                            selectedCityIds.add(city.getId());
                        }
                    } else {
                        selectedCityIds.remove(Integer.valueOf(city.getId()));
                    }
                });

                // Enable drag for non-default cities
                if (!city.isDefault()) {
                    holder.ivDragHandle.setOnTouchListener((v, event) -> {
                        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(holder);
                        }
                        return false;
                    });
                } else {
                    holder.ivDragHandle.setOnTouchListener(null);
                }

                // Long press still works to exit or stay in edit mode
                holder.itemView.setOnLongClickListener(v -> true);
            } else {
                // Normal mode: Hide both checkbox and drag handle
                holder.cbSelectCity.setVisibility(View.GONE);
                holder.ivDragHandle.setVisibility(View.GONE);

                // Long press to enter edit mode
                holder.itemView.setOnLongClickListener(v -> {
                    enterEditMode();
                    return true;
                });
            }
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        class CityViewHolder extends RecyclerView.ViewHolder {
            TextView tvCityName, tvCityWeather, tvCityTemp;
            ImageView ivLocationPin, ivDragHandle;
            CheckBox cbSelectCity;

            public CityViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCityName = itemView.findViewById(R.id.tvCityName);
                tvCityWeather = itemView.findViewById(R.id.tvCityWeather);
                tvCityTemp = itemView.findViewById(R.id.tvCityTemp);
                ivLocationPin = itemView.findViewById(R.id.ivLocationPin);
                ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
                cbSelectCity = itemView.findViewById(R.id.cbSelectCity);
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

                // Fetch weather data before adding to database
                fetchWeatherDataAndAddCity(city);
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

    /**
     * Fetch weather data from Open-Meteo API and then add city to database
     */
    private void fetchWeatherDataAndAddCity(City city) {
        // Show loading indicator (optional)
        Toast.makeText(this, "Fetching weather data...", Toast.LENGTH_SHORT).show();

        // Fetch weather data using coordinates (Open-Meteo doesn't require API key)
        WeatherDataAPI.getDataByCoordinates(
                this,
                city.getLatitude(),
                city.getLongitude(),
                new WeatherDataAPI.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            // Parse weather data from Open-Meteo API response
                            JSONObject weatherJson = new JSONObject(response);

                            // Open-Meteo response structure:
                            // {
                            // "current": {
                            // "temperature_2m": 25.5,
                            // "weather_code": 1,
                            // "relative_humidity_2m": 65,
                            // "pressure_msl": 1013.2,
                            // "sunrise": "2024-01-01T06:00",
                            // "sunset": "2024-01-01T18:00"
                            // },
                            // "daily": {
                            // "temperature_2m_max": [30.0, ...],
                            // "temperature_2m_min": [20.0, ...]
                            // }
                            // }

                            JSONObject current = weatherJson.getJSONObject("current");
                            double temp = current.getDouble("temperature_2m");
                            int humidity = current.optInt("relative_humidity_2m", 0);
                            double pressure = current.optDouble("pressure_msl", 0.0);
                            int weatherCode = current.optInt("weather_code", 0);

                            // Get daily forecast for high/low temps
                            double tempMax = temp;
                            double tempMin = temp;
                            if (weatherJson.has("daily")) {
                                JSONObject daily = weatherJson.getJSONObject("daily");
                                if (daily.has("temperature_2m_max")) {
                                    JSONArray tempMaxArray = daily.getJSONArray("temperature_2m_max");
                                    if (tempMaxArray.length() > 0) {
                                        tempMax = tempMaxArray.getDouble(0);
                                    }
                                }
                                if (daily.has("temperature_2m_min")) {
                                    JSONArray tempMinArray = daily.getJSONArray("temperature_2m_min");
                                    if (tempMinArray.length() > 0) {
                                        tempMin = tempMinArray.getDouble(0);
                                    }
                                }
                            }

                            // Convert weather code to condition string
                            // Open-Meteo uses WMO weather codes (0-99)
                            String weatherCondition = convertWeatherCodeToCondition(weatherCode);

                            // Parse sunrise/sunset (Open-Meteo returns ISO 8601 strings)
                            int sunrise = 0;
                            int sunset = 0;
                            String sunriseStr = current.optString("sunrise", "");
                            String sunsetStr = current.optString("sunset", "");
                            // Note: Open-Meteo returns ISO 8601 format, you may need to parse it
                            // For now, we'll set to 0 if parsing fails

                            // Update city with weather data
                            city.setTemperature(String.valueOf((int) Math.round(temp)));
                            city.setWeatherCondition(weatherCondition);
                            city.setHighTemp(String.valueOf((int) Math.round(tempMax)));
                            city.setLowTemp(String.valueOf((int) Math.round(tempMin)));
                            city.setHumidity(humidity);
                            city.setPressure((int) Math.round(pressure));
                            city.setSunrise(sunrise);
                            city.setSunset(sunset);

                            // Add city to database
                            runOnUiThread(() -> {
                                long id = dbHelper.addCity(city);
                                city.setId((int) id);
                                loadCities();
                                etSearchCity.setText("");
                                rvSearchResults.setVisibility(View.GONE);
                                Toast.makeText(ManageCitiesActivity.this,
                                        R.string.city_added, Toast.LENGTH_SHORT).show();
                            });

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing weather data: " + e.getMessage(), e);
                            // Add city without weather data if parsing fails
                            runOnUiThread(() -> {
                                long id = dbHelper.addCity(city);
                                city.setId((int) id);
                                loadCities();
                                etSearchCity.setText("");
                                rvSearchResults.setVisibility(View.GONE);
                                Toast.makeText(ManageCitiesActivity.this,
                                        R.string.city_added, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error fetching weather data: " + error);
                        // Add city without weather data if API call fails
                        runOnUiThread(() -> {
                            long id = dbHelper.addCity(city);
                            city.setId((int) id);
                            loadCities();
                            etSearchCity.setText("");
                            rvSearchResults.setVisibility(View.GONE);
                            Toast.makeText(ManageCitiesActivity.this,
                                    R.string.city_added, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    /**
     * Convert Open-Meteo weather code (WMO) to condition string
     * WMO Weather codes:
     * https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM
     */
    private String convertWeatherCodeToCondition(int weatherCode) {
        // WMO Weather interpretation codes (WW)
        if (weatherCode == 0)
            return "Clear";
        if (weatherCode <= 3)
            return "Cloudy";
        if (weatherCode <= 49)
            return "Foggy";
        if (weatherCode <= 59)
            return "Drizzle";
        if (weatherCode <= 69)
            return "Rainy";
        if (weatherCode <= 79)
            return "Snowy";
        if (weatherCode <= 84)
            return "Rainy";
        if (weatherCode <= 86)
            return "Snowy";
        if (weatherCode <= 99)
            return "Thunderstorm";
        return "Clear"; // Default
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
