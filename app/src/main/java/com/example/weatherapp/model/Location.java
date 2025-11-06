package com.example.weatherapp.model;

public class Location {
    protected String location;
    protected int[] forecast_in_day;
    protected int sunrise, sunset;
    protected int pressure;
    protected int humidity;
    protected int uv_index;
    protected String uv_level;

    public Location(String location, int[] forecast_in_day, int sunrise, int sunset, int pressure, int humidity, int uv_index, String uv_level) {
        this.location = location;
        this.forecast_in_day = forecast_in_day;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.pressure = pressure;
        this.humidity = humidity;
        this.uv_index = uv_index;
        this.uv_level = uv_level;
    }

//    public get_data_from_api
}
