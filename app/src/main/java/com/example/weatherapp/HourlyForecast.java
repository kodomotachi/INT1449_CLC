package com.example.weatherapp;

public class HourlyForecast {
    private String temperature;
    private String time;
    private String windForce;
    private int iconResource;

    public HourlyForecast(String temperature, String time, String windForce, int iconResource) {
        this.temperature = temperature;
        this.time = time;
        this.windForce = windForce;
        this.iconResource = iconResource;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWindForce() {
        return windForce;
    }

    public void setWindForce(String windForce) {
        this.windForce = windForce;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
}

