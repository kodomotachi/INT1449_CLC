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

    // Convenience helper to fetch weather data by coordinates
    public static void getDataByCoordinates(Context context,
            double latitude,
            double longitude,
            String apiKey,
            ApiCallback callback) {
        String url = String.format(
                Locale.US,
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                latitude,
                longitude,
                apiKey);
        getData(context, url, callback);
    }

    public interface ApiCallback {
        void onSuccess(String response);

        void onError(String error);
    }
}
