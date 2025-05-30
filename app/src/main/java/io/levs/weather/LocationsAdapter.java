package io.levs.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.levs.weather.models.Location;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationViewHolder> {

    private List<Location> locations;
    private final Context context;
    private final LocationClickListener clickListener;
    private final ItemTouchHelper touchHelper;
    private boolean editMode = false;

    public interface LocationClickListener {
        void onLocationClick(Location location);
        void onLocationDelete(Location location, int position);
    }

    public LocationsAdapter(Context context, List<Location> locations, LocationClickListener clickListener, ItemTouchHelper touchHelper) {
        this.context = context;
        this.locations = locations;
        this.clickListener = clickListener;
        this.touchHelper = touchHelper;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locations.get(position);
        
        holder.locationTitle.setText(location.getName());
        holder.locationDetails.setText(location.isCurrentLocation() ? "Current location | " + location.getCondition() : location.getName() + " | " + location.getCondition());
        holder.temperature.setText(location.getTemperature());
        
        holder.locationIcon.setImageResource(location.isCurrentLocation()
                ? R.drawable.ph_map_pin 
                : R.drawable.ph_buildings);
        
        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && !editMode) {
                clickListener.onLocationClick(location);
            }
        });
        
        // Show delete button only in edit mode and for saved locations (not current location)
        boolean showEditControls = editMode && !location.isCurrentLocation();
        holder.deleteButton.setVisibility(showEditControls ? View.VISIBLE : View.GONE);
        holder.deleteButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLocationDelete(location, holder.getAdapterPosition());
            }
        });
        
        // Show drag handle only in edit mode and for saved locations (not current location)
        holder.dragHandle.setVisibility(showEditControls ? View.VISIBLE : View.GONE);
        
        // Set up drag functionality
        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && editMode) {
                touchHelper.startDrag(holder);
                return true; // Consume the event to prevent conflicts
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return locations != null ? locations.size() : 0;
    }
    
    public void updateLocations(List<Location> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }
    
    public void removeLocation(int position) {
        if (position >= 0 && position < locations.size()) {
            locations.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void moveItem(int fromPosition, int toPosition) {
        // Validate positions to avoid IndexOutOfBoundsException
        if (fromPosition < 0 || toPosition < 0 || 
            fromPosition >= locations.size() || 
            toPosition >= locations.size()) {
            return;
        }
        
        Location fromLocation = locations.get(fromPosition);
        if (fromLocation.isCurrentLocation()) {
            return;
        }
        
        for (int i = 0; i < locations.size(); i++) {
            if (i == toPosition && locations.get(i).isCurrentLocation()) {
                return;
            }
        }
        
        Location movedItem = locations.get(fromPosition);
        locations.remove(fromPosition);
        locations.add(toPosition, movedItem);
        
        notifyItemMoved(fromPosition, toPosition);
    }
    
    /**
     * Set edit mode to show/hide drag handles and delete buttons
     * @param editMode true to enable edit mode, false to disable
     */
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }
    
    /**
     * @return current edit mode state
     */
    public boolean isEditMode() {
        return editMode;
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView locationIcon;
        TextView locationTitle;
        TextView locationDetails;
        TextView temperature;
        ImageView deleteButton;
        ImageView dragHandle;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationIcon = itemView.findViewById(R.id.locationIcon);
            locationTitle = itemView.findViewById(R.id.locationTitle);
            locationDetails = itemView.findViewById(R.id.locationDetails);
            temperature = itemView.findViewById(R.id.temperatureText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            dragHandle = itemView.findViewById(R.id.dragHandle);
        }
    }
}
