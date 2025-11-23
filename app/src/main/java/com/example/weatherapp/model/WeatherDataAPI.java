package com.example.weatherapp.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

/*
* Wanna call API to get data, just use method getData with those parameters to get data u want
* "context" is
* */
