package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder> {

    private List<HourlyForecast> hourlyForecasts;

    public HourlyForecastAdapter(List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;
    }

    @NonNull
    @Override
    public HourlyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new HourlyForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyForecastViewHolder holder, int position) {
        HourlyForecast forecast = hourlyForecasts.get(position);
        holder.tvHourlyTemp.setText(forecast.getTemperature());
        holder.tvHourlyTime.setText(forecast.getTime());
        holder.tvHourlyWind.setText(forecast.getWindForce());
        holder.ivHourlyIcon.setImageResource(forecast.getIconResource());
    }

    @Override
    public int getItemCount() {
        return hourlyForecasts.size();
    }

    public static class HourlyForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvHourlyTemp;
        TextView tvHourlyTime;
        TextView tvHourlyWind;
        ImageView ivHourlyIcon;

        public HourlyForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHourlyTemp = itemView.findViewById(R.id.tvHourlyTemp);
            tvHourlyTime = itemView.findViewById(R.id.tvHourlyTime);
            tvHourlyWind = itemView.findViewById(R.id.tvHourlyWind);
            ivHourlyIcon = itemView.findViewById(R.id.ivHourlyIcon);
        }
    }
}



