package com.example.weatherapp.model;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class LocationService {
    // use for cities suggestion
    public void fetchSuggestion(Context context, String location, final NominatimCallback callback) {
        String encodedLocation;

        try {
            encodedLocation = URLEncoder.encode(location, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            callback.onError("Error while encoding location's name");
            return;
        }

        String searchCity = "https://nominatim.openstreetmap.org/search?city=" + encodedLocation
                + "&format=json&limit=5";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                searchCity,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            callback.onSuccess(response);
                        } else {
                            callback.onError("Not found");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Connection error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "My Application/1.0");
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }

    /**
     * Reverse geocoding: Get city name and country from coordinates
     * Uses Nominatim reverse geocoding API
     */
    public void reverseGeocode(Context context, double latitude, double longitude, final NominatimCallback callback) {
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?lat=%.6f&lon=%.6f&format=json&addressdetails=1",
                latitude,
                longitude);

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Convert single object to array format for compatibility
                        JSONArray array = new JSONArray();
                        array.put(response);
                        callback.onSuccess(array);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Connection error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "My Application/1.0");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
}