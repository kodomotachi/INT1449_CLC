package com.example.weatherapp.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class LocationHelper {
    private static final String TAG = "LocationHelper";
    private static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private static final float LOCATION_UPDATE_DISTANCE = 10f; // 10 meters

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    /**
     * Get current GPS location
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission
     */
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        if (locationManager == null) {
            callback.onLocationError("Location service not available");
            return;
        }

        // Check permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED 
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        // Try to get last known location first (faster)
        Location lastKnownLocation = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnownLocation != null) {
            // Use last known location if available and recent (less than 5 minutes old)
            long timeDiff = System.currentTimeMillis() - lastKnownLocation.getTime();
            if (timeDiff < 5 * 60 * 1000) { // 5 minutes
                Log.d(TAG, "Using last known location");
                callback.onLocationReceived(
                    lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude()
                );
                return;
            }
        }

        // Request location update
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    callback.onLocationReceived(
                        location.getLatitude(),
                        location.getLongitude()
                    );
                    // Remove listener after getting location
                    if (locationManager != null) {
                        locationManager.removeUpdates(this);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                callback.onLocationError("Location provider disabled");
                if (locationManager != null) {
                    locationManager.removeUpdates(this);
                }
            }
        };

        // Try GPS first, then network
        boolean locationRequested = false;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    locationListener
                );
                locationRequested = true;
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException requesting GPS location: " + e.getMessage());
            }
        }

        if (!locationRequested && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    locationListener
                );
                locationRequested = true;
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException requesting network location: " + e.getMessage());
            }
        }

        if (!locationRequested) {
            callback.onLocationError("No location provider available");
        }
    }

    /**
     * Check if location permissions are granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if location services are enabled
     */
    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) 
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}

