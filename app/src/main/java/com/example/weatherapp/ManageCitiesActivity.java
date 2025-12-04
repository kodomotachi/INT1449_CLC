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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cities);

        dbHelper = new CityDatabaseHelper(this);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Called when search query changes.
     * Override this method or call from backend to fetch suggestions.
     * @param query The search query string
     */
    protected void onSearchQueryChanged(String query) {
        // TODO: Backend should implement this to fetch city suggestions
        // Example: Call your API with the query and then call onSuggestionsReceived() with results
        Log.d(TAG, "Search query: " + query);
    }

    /**
     * Receives city suggestions from backend.
     * Call this method from your backend/API response handler.
     * 
     * @param suggestions JSONArray of city suggestions (maximum 5 will be displayed)
     * 
     * Expected JSON format:
     * [
     *   {"name": "Singapore", "country": "Singapore", "lat": 1.3521, "lon": 103.8198},
     *   {"name": "Sydney", "country": "Australia", "lat": -33.8688, "lon": 151.2093},
     *   ...
     * ]
     */
    public void onSuggestionsReceived(JSONArray suggestions) {
        runOnUiThread(() -> {
            searchResults.clear();
            
            if (suggestions == null || suggestions.length() == 0) {
                rvSearchResults.setVisibility(View.GONE);
                searchResultsAdapter.notifyDataSetChanged();
                return;
            }
            
            // Parse up to MAX_SUGGESTIONS (5) items
            int count = Math.min(suggestions.length(), MAX_SUGGESTIONS);
            
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
                    }
                    
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing city at index " + i + ": " + e.getMessage());
                }
            }
            
            if (searchResults.isEmpty()) {
                rvSearchResults.setVisibility(View.GONE);
            } else {
                rvSearchResults.setVisibility(View.VISIBLE);
            }
            searchResultsAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Convenience method to receive suggestions as a JSON string.
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
        
        // Add sample weather data
        for (City city : cities) {
            city.setTemperature("28");
            city.setWeatherCondition("Cloudy");
            city.setHighTemp("30");
            city.setLowTemp("24");
        }
        
        citiesAdapter.notifyDataSetChanged();
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
            holder.tvCityWeather.setText(city.getWeatherCondition() + "  " + 
                    city.getHighTemp() + "° / " + city.getLowTemp() + "°");
            holder.tvCityTemp.setText(city.getTemperature() + "°");

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
