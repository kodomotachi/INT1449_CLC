package com.example.weatherapp;

public class City {
    private String name;
    private String country;
    private boolean isDefault; // Default city cannot be deleted
    private double latitude;
    private double longitude;

    public City(String name, String country, boolean isDefault) {
        this.name = name;
        this.country = country;
        this.isDefault = isDefault;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public City(String name, String country, boolean isDefault, double latitude, double longitude) {
        this.name = name;
        this.country = country;
        this.isDefault = isDefault;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFullName() {
        return name + ", " + country;
    }
}

