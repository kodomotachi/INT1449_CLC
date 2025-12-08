package com.example.weatherapp.model;

/**
 * City domain model: holds identity, coordinates, default flag,
 * and optional weather snapshot used across the app.
 */
public class City {
	private int id;
	private String name;
	private String country;
	private double latitude;
	private double longitude;
	private boolean isDefault;

	// Basic weather snapshot (shown in lists/cards)
	private String temperature;
	private String weatherCondition;
	private String highTemp;
	private String lowTemp;

	// Optional detail metrics (replaces the old Location fields)
	private int[] forecastInDay;
	private int sunrise;
	private int sunset;
	private int pressure;
	private int humidity;
	private int uvIndex;
	private String uvLevel;

	public City() {
	}

	public City(String name, String country, double latitude, double longitude) {
		this(-1, name, country, latitude, longitude, false);
	}

	public City(int id, String name, String country, double latitude, double longitude, boolean isDefault) {
		this.id = id;
		this.name = name;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
		this.isDefault = isDefault;
	}

	// Identity / geo
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	// Weather snapshot
	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getWeatherCondition() {
		return weatherCondition;
	}

	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}

	public String getHighTemp() {
		return highTemp;
	}

	public void setHighTemp(String highTemp) {
		this.highTemp = highTemp;
	}

	public String getLowTemp() {
		return lowTemp;
	}

	public void setLowTemp(String lowTemp) {
		this.lowTemp = lowTemp;
	}

	public void setWeatherSnapshot(String temperature, String weatherCondition, String highTemp, String lowTemp) {
		this.temperature = temperature;
		this.weatherCondition = weatherCondition;
		this.highTemp = highTemp;
		this.lowTemp = lowTemp;
	}

	// Detailed metrics (optional)
	public int[] getForecastInDay() {
		return forecastInDay;
	}

	public void setForecastInDay(int[] forecastInDay) {
		this.forecastInDay = forecastInDay;
	}

	public int getSunrise() {
		return sunrise;
	}

	public void setSunrise(int sunrise) {
		this.sunrise = sunrise;
	}

	public int getSunset() {
		return sunset;
	}

	public void setSunset(int sunset) {
		this.sunset = sunset;
	}

	public int getPressure() {
		return pressure;
	}

	public void setPressure(int pressure) {
		this.pressure = pressure;
	}

	public int getHumidity() {
		return humidity;
	}

	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	public int getUvIndex() {
		return uvIndex;
	}

	public void setUvIndex(int uvIndex) {
		this.uvIndex = uvIndex;
	}

	public String getUvLevel() {
		return uvLevel;
	}

	public void setUvLevel(String uvLevel) {
		this.uvLevel = uvLevel;
	}

	public String getDisplayName() {
		if (country != null && !country.isEmpty()) {
			return name + ", " + country;
		}
		return name;
	}
}
