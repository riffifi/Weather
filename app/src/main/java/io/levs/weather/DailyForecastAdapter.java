package io.levs.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.levs.weather.models.DailyForecast;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {

    private List<DailyForecast> dailyForecasts;

    public DailyForecastAdapter(List<DailyForecast> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyForecast forecast = dailyForecasts.get(position);
        holder.dayTextView.setText(forecast.getDay());
        holder.highTempTextView.setText(forecast.getHighTemp());
        holder.lowTempTextView.setText(forecast.getLowTemp());
        holder.weatherIconImageView.setImageResource(forecast.getIconResource());
    }

    @Override
    public int getItemCount() {
        return dailyForecasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dayTextView;
        public TextView highTempTextView;
        public TextView lowTempTextView;
        public ImageView weatherIconImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayOfWeekTextView);
            highTempTextView = itemView.findViewById(R.id.maxTempTextView);
            lowTempTextView = itemView.findViewById(R.id.minTempTextView);
            weatherIconImageView = itemView.findViewById(R.id.weatherIconImageView);
        }
    }
} 