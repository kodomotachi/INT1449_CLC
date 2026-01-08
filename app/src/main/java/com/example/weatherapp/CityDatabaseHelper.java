package com.example.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.weatherapp.model.City;

import java.util.ArrayList;
import java.util.List;

public class CityDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather_cities.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CITIES = "cities";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COUNTRY = "country";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_IS_DEFAULT = "is_default";

    public CityDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CITIES_TABLE = "CREATE TABLE " + TABLE_CITIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_COUNTRY + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_IS_DEFAULT + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_CITIES_TABLE);

        // Add default city (Binh Tan, Ho Chi Minh City, Vietnam)
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, "Binh Tan");
        values.put(COLUMN_COUNTRY, "Vietnam");
        values.put(COLUMN_LATITUDE, 10.7333);
        values.put(COLUMN_LONGITUDE, 106.6167);
        values.put(COLUMN_IS_DEFAULT, 1);
        db.insert(TABLE_CITIES, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
        onCreate(db);
    }

    public long addCity(City city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, city.getName());
        values.put(COLUMN_COUNTRY, city.getCountry());
        values.put(COLUMN_LATITUDE, city.getLatitude());
        values.put(COLUMN_LONGITUDE, city.getLongitude());
        values.put(COLUMN_IS_DEFAULT, city.isDefault() ? 1 : 0);

        long id = db.insert(TABLE_CITIES, null, values);
        db.close();
        return id;
    }

    public boolean cityExists(String cityName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CITIES,
                new String[] { COLUMN_ID },
                COLUMN_NAME + "=?",
                new String[] { cityName },
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public City getCity(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CITIES,
                new String[] { COLUMN_ID, COLUMN_NAME, COLUMN_COUNTRY, COLUMN_LATITUDE, COLUMN_LONGITUDE,
                        COLUMN_IS_DEFAULT },
                COLUMN_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null);

        City city = null;
        if (cursor != null && cursor.moveToFirst()) {
            city = new City(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    cursor.getInt(5) == 1);
            cursor.close();
        }
        db.close();
        return city;
    }

    public List<City> getAllCities() {
        List<City> cities = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CITIES + " ORDER BY " + COLUMN_IS_DEFAULT + " DESC, " + COLUMN_ID
                + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                City city = new City(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getDouble(4),
                        cursor.getInt(5) == 1);
                cities.add(city);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cities;
    }

    public void deleteCity(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CITIES, COLUMN_ID + "=?", new String[] { String.valueOf(id) });
        db.close();
    }

    public City getDefaultCity() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CITIES,
                new String[] { COLUMN_ID, COLUMN_NAME, COLUMN_COUNTRY, COLUMN_LATITUDE, COLUMN_LONGITUDE,
                        COLUMN_IS_DEFAULT },
                COLUMN_IS_DEFAULT + "=?",
                new String[] { "1" },
                null, null, null, null);

        City city = null;
        if (cursor != null && cursor.moveToFirst()) {
            city = new City(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    true);
            cursor.close();
        }
        db.close();
        return city;
    }

    /**
     * Update existing city to be default and set all others to non-default
     */
    public void setDefaultCity(int cityId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Set all cities to non-default first
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DEFAULT, 0);
        db.update(TABLE_CITIES, values, null, null);

        // Set the specified city as default
        values = new ContentValues();
        values.put(COLUMN_IS_DEFAULT, 1);
        db.update(TABLE_CITIES, values, COLUMN_ID + "=?", new String[] { String.valueOf(cityId) });

        db.close();
    }

    /**
     * Check if a city with given coordinates already exists
     */
    public City getCityByCoordinates(double latitude, double longitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Use approximate matching (within 0.01 degrees, ~1km)
        String selectQuery = "SELECT * FROM " + TABLE_CITIES
                + " WHERE ABS(" + COLUMN_LATITUDE + " - ?) < 0.01"
                + " AND ABS(" + COLUMN_LONGITUDE + " - ?) < 0.01"
                + " LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {
                String.valueOf(latitude),
                String.valueOf(longitude)
        });

        City city = null;
        if (cursor != null && cursor.moveToFirst()) {
            city = new City(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    cursor.getInt(5) == 1);
            cursor.close();
        }
        db.close();
        return city;
    }
}
