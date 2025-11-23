package com.example.weatherapp.model;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class WeatherDataAPI {

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
                }
        );

        queue.add(request);
    }

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}

// how to use in MainActivity.java

//LocationService locationService = new LocationService();
//String city = "Ha Noi";
//
//        locationService.searchLocation(this, city, new NominatimCallback() {
//    @Override
//    public void onSuccess(JSONArray result) {
//        try {
//            org.json.JSONObject place = result.getJSONObject(0);
//            String displayName = place.getString("display_name");
//            String lat = place.getString("lat");
//            String lon = place.getString("lon");
//            Log.d("API_RESULT", "Name: " + displayName);
//            Log.d("API_RESULT", "Latitude: " + lat);
//            Log.d("API_RESULT", "Longitude: " + lon);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onError(String message) {
//        Log.e("API_ERROR", message);
//    }
//});
