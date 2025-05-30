package io.levs.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.levs.weather.models.Location;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {

    private final List<SearchResultItem> searchResults;
    private final Context context;
    private final SearchResultClickListener clickListener;

    public interface SearchResultClickListener {
        void onResultClick(SearchResultItem item);
        void onAddClick(SearchResultItem item);
    }

    public static class SearchResultItem {
        private final Location location;
        private final boolean isSaved;

        public SearchResultItem(Location location, boolean isSaved) {
            this.location = location;
            this.isSaved = isSaved;
        }

        public Location getLocation() {
            return location;
        }

        public boolean isSaved() {
            return isSaved;
        }
    }

    public SearchResultsAdapter(Context context, SearchResultClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.searchResults = new ArrayList<>();
    }

    public void setSearchResults(List<SearchResultItem> results) {
        this.searchResults.clear();
        this.searchResults.addAll(results);
        notifyDataSetChanged();
    }

    public void clearResults() {
        this.searchResults.clear();
        notifyDataSetChanged();
    }
    
    /**
     * Update the saved status of an item with the given location ID
     * @param locationId The ID of the location to update
     * @param isSaved The new saved status
     * @return true if an item was updated, false otherwise
     */
    public boolean updateItemSavedStatus(String locationId, boolean isSaved) {
        for (int i = 0; i < searchResults.size(); i++) {
            SearchResultItem item = searchResults.get(i);
            if (item.getLocation().getId().equals(locationId)) {
                // Create a new item with the updated saved status
                SearchResultItem updatedItem = new SearchResultItem(item.getLocation(), isSaved);
                searchResults.set(i, updatedItem);
                notifyItemChanged(i);
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResultItem item = searchResults.get(position);
        Location location = item.getLocation();
        
        holder.locationTitle.setText(location.getName());
        
        if (location.getTemperature() != null && !location.getTemperature().equals("--°") &&
            location.getCondition() != null && !location.getCondition().equals("Unknown")) {
            String weatherInfo = location.getTemperature() + " • " + location.getCondition();
            holder.locationStatus.setText(weatherInfo);
            holder.locationStatus.setVisibility(View.VISIBLE);
        } else {
            holder.locationStatus.setVisibility(View.GONE);
        }
        
        if (item.isSaved()) {
            holder.actionButton.setImageResource(R.drawable.ph_check_circle);
            holder.actionButton.setContentDescription("Already saved");
            holder.actionButton.setEnabled(false);
            holder.actionButton.setAlpha(0.5f);
        } else {
            holder.actionButton.setImageResource(R.drawable.ph_plus_circle);
            holder.actionButton.setContentDescription("Add location");
            holder.actionButton.setEnabled(true);
            holder.actionButton.setAlpha(1.0f);
        }
        
        holder.itemView.setOnClickListener(v -> clickListener.onResultClick(item));
        holder.actionButton.setOnClickListener(v -> {
            if (!item.isSaved()) {
                clickListener.onAddClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView locationTitle;
        TextView locationStatus;
        ImageButton actionButton;

        SearchResultViewHolder(View itemView) {
            super(itemView);
            locationTitle = itemView.findViewById(R.id.locationTitle);
            locationStatus = itemView.findViewById(R.id.locationStatus);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}
