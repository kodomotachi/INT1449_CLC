package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private List<City> cities;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(City city, int position);
        void onDeleteClick(City city, int position);
    }

    public CityAdapter(List<City> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        City city = cities.get(position);
        
        holder.tvCityName.setText(city.getName());
        holder.tvWeatherInfo.setText("Cloudy  30° / 24°"); // Sample data
        holder.tvCurrentTemp.setText("28°"); // Sample data
        
        // Show location pin for default city
        if (city.isDefault()) {
            holder.ivLocationPin.setVisibility(View.VISIBLE);
            holder.btnDeleteCity.setVisibility(View.GONE);
        } else {
            holder.ivLocationPin.setVisibility(View.GONE);
            holder.btnDeleteCity.setVisibility(View.VISIBLE);
        }
        
        // Click on card to view city weather
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city, position);
            }
        });
        
        // Click on delete button
        holder.btnDeleteCity.setOnClickListener(v -> {
            if (listener != null && !city.isDefault()) {
                listener.onDeleteClick(city, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public void updateCities(List<City> newCities) {
        this.cities = newCities;
        notifyDataSetChanged();
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        TextView tvWeatherInfo;
        TextView tvCurrentTemp;
        ImageView ivLocationPin;
        ImageButton btnDeleteCity;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            tvWeatherInfo = itemView.findViewById(R.id.tvWeatherInfo);
            tvCurrentTemp = itemView.findViewById(R.id.tvCurrentTemp);
            ivLocationPin = itemView.findViewById(R.id.ivLocationPin);
            btnDeleteCity = itemView.findViewById(R.id.btnDeleteCity);
        }
    }
}

