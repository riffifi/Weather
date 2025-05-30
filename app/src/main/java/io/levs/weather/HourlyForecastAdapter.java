package io.levs.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.levs.weather.models.HourlyForecast;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {

    private List<HourlyForecast> hourlyForecasts;

    public HourlyForecastAdapter(List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyForecast forecast = hourlyForecasts.get(position);
        holder.timeTextView.setText(forecast.getTime());
        holder.temperatureTextView.setText(forecast.getTemperature());
        holder.weatherIconImageView.setImageResource(forecast.getIconResource());
        // holder.precipitationTextView.setText("5%");
    }

    @Override
    public int getItemCount() {
        return hourlyForecasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTextView;
        public TextView temperatureTextView;
        public ImageView weatherIconImageView;
        public TextView precipitationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            temperatureTextView = itemView.findViewById(R.id.temperatureTextView);
            weatherIconImageView = itemView.findViewById(R.id.weatherIconImageView);
            precipitationTextView = itemView.findViewById(R.id.precipitationTextView);
        }
    }
} 