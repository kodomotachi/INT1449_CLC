package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.model.City;
import com.example.weatherapp.model.WeatherDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherPageFragment extends Fragment {

    private static final String ARG_CITY = "city";
    private static final String TAG = "WeatherPageFragment";

    private City city;
    private TextView tvLocationName;
    private TextView tvCurrentTemp;
    private TextView tvWeatherCondition;
    private RecyclerView rvHourlyForecast;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private final List<HourlyForecast> hourlyForecasts = new ArrayList<>();

    private TextView tvUvValue;
    private TextView tvHumidityValue;
    private TextView tvRealFeelValue;
    private TextView tvWindValue;
    private TextView tvWindDirection;
    private TextView tvSunsetValue;
    private TextView tvSunriseSmall;
    private TextView tvSunsetSmall;
    private TextView tvPressureValue;

    private ProgressBar pbUv;
    private ProgressBar pbHumidity;
    private ProgressBar pbRealFeel;
    private ImageView ivWindNeedle;

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
                    getArguments().getBoolean("city_default"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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
        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast);

        tvUvValue = view.findViewById(R.id.tvUvValue);
        tvHumidityValue = view.findViewById(R.id.tvHumidityValue);
        tvRealFeelValue = view.findViewById(R.id.tvRealFeelValue);
        tvWindValue = view.findViewById(R.id.tvWindValue);
        tvWindDirection = view.findViewById(R.id.tvWindDirection);
        tvSunsetValue = view.findViewById(R.id.tvSunsetValue);
        tvSunriseSmall = view.findViewById(R.id.tvSunriseSmall);
        tvSunsetSmall = view.findViewById(R.id.tvSunsetSmall);
        tvPressureValue = view.findViewById(R.id.tvPressureValue);

        pbUv = view.findViewById(R.id.pbUv);
        pbHumidity = view.findViewById(R.id.pbHumidity);
        pbRealFeel = view.findViewById(R.id.pbRealFeel);
        ivWindNeedle = view.findViewById(R.id.ivWindNeedle);
    }

    private void setupUI() {
        if (city != null) {
            tvLocationName.setText(city.getName());
        }

        // Load actual weather data from API for this city and update UI
        fetchAndBindWeather();
    }

    private void fetchAndBindWeather() {
        if (getContext() == null || city == null) {
            return;
        }

        WeatherDataAPI.getDataByCoordinates(
                getContext(),
                city.getLatitude(),
                city.getLongitude(),
                new WeatherDataAPI.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject root = new JSONObject(response);
                            bindWeatherResponse(root);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing weather data: " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error fetching weather data: " + error);
                    }
                });
    }

    private void bindWeatherResponse(JSONObject root) throws JSONException {
        JSONObject current = root.optJSONObject("current");
        JSONObject daily = root.optJSONObject("daily");
        JSONObject hourly = root.optJSONObject("hourly");

        if (current == null) {
            return;
        }


        double tempC = current.optDouble("temperature_2m", Double.NaN);
        double apparentC = current.optDouble("apparent_temperature", tempC);
        int humidity = current.optInt("relative_humidity_2m", 0);
        double pressure = current.optDouble("pressure_msl", 0.0);
        int weatherCode = current.optInt("weather_code", 0);
        double windSpeedKmh = current.optDouble("wind_speed_10m", 0.0);
        double windDir = current.optDouble("wind_direction_10m", 0.0);
        String currentTime = current.optString("time", "");

        // Daily high/low and sunrise/sunset
        double tempMaxC = tempC;
        double tempMinC = tempC;
        String sunriseIso = "";
        String sunsetIso = "";
        int uvDailyMax = 0;
        if (daily != null) {
            JSONArray maxArr = daily.optJSONArray("temperature_2m_max");
            JSONArray minArr = daily.optJSONArray("temperature_2m_min");
            if (maxArr != null && maxArr.length() > 0) {
                tempMaxC = maxArr.optDouble(0, tempC);
            }
            if (minArr != null && minArr.length() > 0) {
                tempMinC = minArr.optDouble(0, tempC);
            }

            JSONArray sunriseArr = daily.optJSONArray("sunrise");
            JSONArray sunsetArr = daily.optJSONArray("sunset");
            if (sunriseArr != null && sunriseArr.length() > 0) {
                sunriseIso = sunriseArr.optString(0, "");
            }
            if (sunsetArr != null && sunsetArr.length() > 0) {
                sunsetIso = sunsetArr.optString(0, "");
            }

            JSONArray uvMaxArr = daily.optJSONArray("uv_index_max");
            if (uvMaxArr != null && uvMaxArr.length() > 0) {
                uvDailyMax = (int) Math.round(uvMaxArr.optDouble(0, 0.0));
            }
        }

        // UV: prefer current.uv_index if present; otherwise map hourly uv_index to current time; fallback to daily max
        int uvIndex = 0;
        if (current.has("uv_index")) {
            uvIndex = (int) Math.round(current.optDouble("uv_index", 0.0));
        } else if (hourly != null) {
            JSONArray times = hourly.optJSONArray("time");
            JSONArray uvs = hourly.optJSONArray("uv_index");
            if (times != null && uvs != null) {
                int match = -1;
                for (int i = 0; i < times.length(); i++) {
                    if (currentTime.equals(times.optString(i, ""))) {
                        match = i;
                        break;
                    }
                }
                if (match >= 0) {
                    uvIndex = (int) Math.round(uvs.optDouble(match, 0.0));
                } else if (uvs.length() > 0) {
                    uvIndex = (int) Math.round(uvs.optDouble(0, 0.0));
                }
            }
        } else {
            uvIndex = uvDailyMax;
        }

        final int uvIndexFinal = clampInt(uvIndex, 0, 11);
        final int humidityFinal = clampInt(humidity, 0, 100);
        final int pressureHpa = (int) Math.round(pressure);
        final float windDirFinal = (float) windDir;
        final String conditionFinal = convertWeatherCodeToCondition(weatherCode);
        final String highFinal = formatTempNoUnit(tempMaxC);
        final String lowFinal = formatTempNoUnit(tempMinC);
        final int realFeelProgressFinal = (int) Math.round(normalizeToPercent(apparentC, tempMinC, tempMaxC));
        final String sunriseHHmmFinal = isoToHHmm(sunriseIso);
        final String sunsetHHmmFinal = isoToHHmm(sunsetIso);

        final List<HourlyForecast> hourlyListFinal = build24HourForecasts(hourly, currentTime);

        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            // Top summary
            if (!Double.isNaN(tempC)) {
                tvCurrentTemp.setText(formatTempNoUnit(tempC));
            } else {
                tvCurrentTemp.setText("--°");
            }

            tvWeatherCondition.setText(conditionFinal + "  " + highFinal + "/" + lowFinal);

            // UV
            tvUvValue.setText(uvLevel(uvIndexFinal));
            if (pbUv != null) {
                // pbUv max=110, progress=uv*10
                pbUv.setProgress(uvIndexFinal * 10);
            }

            // Humidity
            if (humidityFinal > 0) {
                tvHumidityValue.setText(humidityFinal + "%");
                if (pbHumidity != null) {
                    pbHumidity.setProgress(humidityFinal);
                }
            } else {
                tvHumidityValue.setText("--%");
                if (pbHumidity != null) {
                    pbHumidity.setProgress(0);
                }
            }

            // Real feel (apparent temp)
            if (!Double.isNaN(apparentC)) {
                tvRealFeelValue.setText(formatTempNoUnit(apparentC));
            } else {
                tvRealFeelValue.setText("--°");
            }
            if (pbRealFeel != null) {
                pbRealFeel.setProgress(realFeelProgressFinal);
            }

            // Wind
            if (windSpeedKmh > 0) {
                tvWindValue.setText(UnitConverter.formatWindSpeed(getContext(), windSpeedKmh));
                tvWindDirection.setText(windDirectionToCardinal(windDirFinal));
                if (ivWindNeedle != null) {
                    ivWindNeedle.setRotation(windDirFinal);
                }
            } else {
                tvWindValue.setText("--");
                tvWindDirection.setText("--");
            }

            // Sunrise / Sunset
            if (!sunsetHHmmFinal.isEmpty()) {
                tvSunsetValue.setText(sunsetHHmmFinal);
            }
            if (tvSunriseSmall != null) {
                tvSunriseSmall.setText(sunriseHHmmFinal.isEmpty() ? "--:--" : sunriseHHmmFinal);
            }
            if (tvSunsetSmall != null) {
                tvSunsetSmall.setText(sunsetHHmmFinal.isEmpty() ? "--:--" : sunsetHHmmFinal);
            }

            // Pressure
            if (pressureHpa > 0) {
                tvPressureValue.setText(String.valueOf(pressureHpa));
            } else {
                tvPressureValue.setText("--");
            }

            if (hourlyForecastAdapter != null) {
                if (hourlyListFinal != null && !hourlyListFinal.isEmpty()) {
                    Log.d(TAG, "Updating hourly forecast adapter with " + hourlyListFinal.size() + " items");
                    hourlyForecastAdapter.setHourlyForecasts(hourlyListFinal);
                } else {
                    Log.w(TAG, "Hourly forecast list is empty or null");
                }
            } else {
                Log.e(TAG, "Hourly forecast adapter is null!");
            }
        });
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double normalizeToPercent(double value, double min, double max) {
        if (Double.isNaN(value)) return 0.0;
        if (max <= min) return 0.0;
        double t = (value - min) / (max - min);
        t = Math.max(0.0, Math.min(1.0, t));
        return t * 100.0;
    }

    private String formatTempNoUnit(double celsius) {
        if (getContext() == null) return "--°";
        String unit = SettingsActivity.getTemperatureUnit(getContext());
        double display = unit.equals(SettingsActivity.TEMP_FAHRENHEIT)
                ? UnitConverter.celsiusToFahrenheit(celsius)
                : celsius;
        return String.format("%.0f%s", display, getString(R.string.degree_symbol));
    }

    private static String isoToHHmm(String iso) {
        if (iso == null) return "";
        int t = iso.indexOf('T');
        if (t >= 0 && iso.length() >= t + 6) {
            return iso.substring(t + 1, t + 6);
        }
        // already might be HH:MM
        if (iso.length() >= 5 && iso.charAt(2) == ':') {
            return iso.substring(0, 5);
        }
        return "";
    }

    private static String uvLevel(int uv) {
        if (uv <= 2) return "Low";
        if (uv <= 5) return "Moderate";
        if (uv <= 7) return "High";
        if (uv <= 10) return "Very High";
        return "Extreme";
    }

    private static String windDirectionToCardinal(float degrees) {
        // 8-point compass
        float d = (degrees % 360 + 360) % 360;
        if (d >= 337.5 || d < 22.5) return "North";
        if (d < 67.5) return "North-East";
        if (d < 112.5) return "East";
        if (d < 157.5) return "South-East";
        if (d < 202.5) return "South";
        if (d < 247.5) return "South-West";
        if (d < 292.5) return "West";
        return "North-West";
    }

    private List<HourlyForecast> build24HourForecasts(JSONObject hourly, String currentTimeIso) {
        if (hourly == null) {
            Log.w(TAG, "Hourly data is null");
            return new ArrayList<>();
        }

        JSONArray times = hourly.optJSONArray("time");
        JSONArray temps = hourly.optJSONArray("temperature_2m");
        JSONArray codes = hourly.optJSONArray("weather_code");
        JSONArray windSpeeds = hourly.optJSONArray("wind_speed_10m");
        
        if (times == null || temps == null || codes == null || windSpeeds == null) {
            Log.w(TAG, "Hourly arrays are null - times: " + (times != null) + 
                      ", temps: " + (temps != null) + 
                      ", codes: " + (codes != null) + 
                      ", windSpeeds: " + (windSpeeds != null));
            return new ArrayList<>();
        }
        
        Log.d(TAG, "Building 24h forecast - times count: " + times.length() + 
                  ", currentTime: " + currentTimeIso);

        int start = 0;
        
        // Find the index of current time, then start from the next hour
        if (currentTimeIso != null && !currentTimeIso.isEmpty() && times.length() > 0) {
            long currentTimeMillis = parseIsoToMillis(currentTimeIso);
            if (currentTimeMillis > 0) {
                long minDiff = Long.MAX_VALUE;
                int currentIndex = -1;
                
                // Find the index closest to current time
                for (int i = 0; i < times.length(); i++) {
                    String timeStr = times.optString(i, "");
                    long timeMillis = parseIsoToMillis(timeStr);
                    if (timeMillis > 0) {
                        long diff = Math.abs(timeMillis - currentTimeMillis);
                        if (diff < minDiff) {
                            minDiff = diff;
                            currentIndex = i;
                        }
                        // Also check for exact match
                        if (currentTimeIso.equals(timeStr)) {
                            currentIndex = i;
                            break;
                        }
                    }
                }
                
                // Start from the next hour after current time
                if (currentIndex >= 0 && currentIndex + 1 < times.length()) {
                    start = currentIndex + 1;
                } else if (currentIndex >= 0) {
                    // If we're at the last index, start from there
                    start = currentIndex;
                }
            }
        }

        // Ensure we always show a full set of 24 hours (next 24h). If we're too close to the end,
        // shift the window back so we can still render 24 items.
        if (times.length() >= 24 && start + 24 > times.length()) {
            start = Math.max(0, times.length() - 24);
        }

        int end = Math.min(start + 24, times.length());
        List<HourlyForecast> list = new ArrayList<>();
        Log.d(TAG, "Building forecast from index " + start + " to " + end + " (starting from next hour)");
        
        for (int i = start; i < end; i++) {
            double tempC = temps.optDouble(i, Double.NaN);
            String tempStr = Double.isNaN(tempC) ? "--°" : formatTempNoUnit(tempC);

            // Always show time in HH:mm format (no "Now" label since we start from next hour)
            String timeLabel = isoToHHmm(times.optString(i, ""));

            double windKmh = windSpeeds.optDouble(i, 0.0);
            String windStr = UnitConverter.formatWindSpeed(getContext(), windKmh);

            int code = codes.optInt(i, 0);
            int iconRes = weatherCodeToIconRes(code);

            list.add(new HourlyForecast(tempStr, timeLabel, windStr, iconRes));
        }
        
        Log.d(TAG, "Built " + list.size() + " hourly forecast items");
        return list;
    }

    /**
     * Parse ISO 8601 datetime string to milliseconds since epoch.
     * Format: "2024-01-01T12:00" or "2024-01-01T12:00:00" or "2024-01-01T12:00:00Z"
     */
    private long parseIsoToMillis(String iso) {
        if (iso == null || iso.isEmpty()) return 0;
        try {
            // Remove timezone info if present (Z or +HH:MM)
            String cleanIso = iso;
            int zIndex = cleanIso.indexOf('Z');
            if (zIndex > 0) {
                cleanIso = cleanIso.substring(0, zIndex);
            }
            int plusIndex = cleanIso.indexOf('+');
            if (plusIndex > 0) {
                cleanIso = cleanIso.substring(0, plusIndex);
            }
            int minusIndex = cleanIso.indexOf('-', 10); // Skip date part
            if (minusIndex > 0) {
                cleanIso = cleanIso.substring(0, minusIndex);
            }
            
            // Ensure we have at least "YYYY-MM-DDTHH:MM"
            if (cleanIso.length() < 16) return 0;
            
            // Parse: "YYYY-MM-DDTHH:MM" or "YYYY-MM-DDTHH:MM:SS"
            java.text.SimpleDateFormat sdf;
            if (cleanIso.length() >= 19) {
                sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            } else {
                sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.US);
            }
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.parse(cleanIso).getTime();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing ISO datetime: " + iso, e);
            return 0;
        }
    }

    private int weatherCodeToIconRes(int weatherCode) {
        // WMO weather codes (Open-Meteo)
        if (weatherCode == 0) return R.drawable.ic_sun; // clear
        if (weatherCode <= 3) return R.drawable.ic_cloud; // cloudy
        if (weatherCode <= 49) return R.drawable.ic_cloud; // fog
        if (weatherCode <= 69) return R.drawable.ic_rain; // drizzle / rain
        if (weatherCode <= 79) return R.drawable.ic_snow; // snow
        if (weatherCode <= 84) return R.drawable.ic_rain; // rain showers
        if (weatherCode <= 86) return R.drawable.ic_snow; // snow showers
        if (weatherCode <= 99) return R.drawable.ic_rain; // thunderstorm
        return R.drawable.ic_cloud;
    }

    private void setupHourlyForecast() {
        if (getContext() == null || rvHourlyForecast == null) {
            Log.e(TAG, "Cannot setup hourly forecast - context or RecyclerView is null");
            return;
        }
        
        hourlyForecastAdapter = new HourlyForecastAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvHourlyForecast.setLayoutManager(layoutManager);
        rvHourlyForecast.setAdapter(hourlyForecastAdapter);
        Log.d(TAG, "Hourly forecast RecyclerView setup completed");
    }

    /**
     * Convert Open-Meteo weather code (WMO) to condition string.
     */
    private String convertWeatherCodeToCondition(int weatherCode) {
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
        return "Clear";
    }
}
