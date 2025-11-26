package com.example.weatherapp.model;

import org.json.JSONArray;

public interface NominatimCallback {
    void onSuccess(JSONArray result);
    void onError(String message);
}
