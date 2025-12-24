package com.example.weatherapp.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Locale;

public class WeatherDataAPI {

    // Base URL for Open-Meteo API (no API key required)
    private static final String OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/forecast";

    // use for both url with non-coordinate and coordinate
    public static void getData(Context context, String url, ApiCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.toString());
                    }
                });

        queue.add(request);
    }

    /**
     * Fetch current weather data from Open-Meteo API by coordinates
     * Open-Meteo is free and doesn't require an API key
     * 
     * @param context Android context
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback Callback to handle response
     */
    public static void getDataByCoordinates(Context context,
            double latitude,
            double longitude,
            ApiCallback callback) {
        // Open-Meteo API endpoint with current weather and daily forecast parameters
        // Current: temperature_2m, apparent_temperature, weather_code, relative_humidity_2m,
        //          pressure_msl, wind_speed_10m, wind_direction_10m
        // Hourly: temperature_2m, weather_code, wind_speed_10m, uv_index
        // (used to render 24-hour forecast icons + to get "current" UV if current.uv_index is not present)
        // Daily: sunrise, sunset, temperature_2m_max, temperature_2m_min, uv_index_max
        String url = String.format(
                Locale.US,
                "%s?latitude=%.4f&longitude=%.4f"
                        + "&current=temperature_2m,apparent_temperature,weather_code,relative_humidity_2m,pressure_msl,wind_speed_10m,wind_direction_10m"
                        + "&hourly=temperature_2m,weather_code,wind_speed_10m,uv_index"
                        + "&daily=sunrise,sunset,temperature_2m_max,temperature_2m_min,uv_index_max"
                        + "&temperature_unit=celsius&wind_speed_unit=kmh"
                        + "&timezone=auto&forecast_days=2",
                OPEN_METEO_BASE_URL,
                latitude,
                longitude);
        getData(context, url, callback);
    }

    public interface ApiCallback {
        void onSuccess(String response);

        void onError(String error);
    }
}
