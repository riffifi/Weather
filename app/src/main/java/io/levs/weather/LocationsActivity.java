package io.levs.weather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import io.levs.weather.api.WeatherRepository;
import io.levs.weather.models.Location;


public class LocationsActivity extends AppCompatActivity implements 
        LocationsAdapter.LocationClickListener, 
        SearchResultsAdapter.SearchResultClickListener {

    private static final String TAG = "LocationsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "io.levs.weather.prefs";
    private SharedPreferences preferences;
    private WeatherRepository repository;
    private Vibrator vibrator;
    private FusedLocationProviderClient fusedLocationClient;

    // UI Components
    private ImageButton backButton;
    private ImageButton editButton;
    private MaterialCardView searchCardView;
    private MaterialCardView myLocationCardView;
    private RecyclerView locationsRecyclerView;
    private TextView emptyStateMessage;
    private TextView savedLocationsTitle;
    private TextView screenTitle;
    
    // Search UI
    private EditText searchEditText;
    private ImageButton clearButton;
    private ConstraintLayout searchResultsContainer;
    private ConstraintLayout savedLocationsContainer;
    private RecyclerView searchResultsRecyclerView;
    private TextView noResultsText;
    private ProgressBar searchProgressBar;
    
    // Adapters and Data
    private LocationsAdapter locationsAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    private List<Location> savedLocations;
    private ItemTouchHelper itemTouchHelper;
    private boolean isEditMode = false;
    private boolean isSearchMode = false;

    // Delayed search handler
    private final Handler searchHandler = new Handler();
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            performSearch(searchEditText.getText().toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_new);
        initViews();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initData();
        setupRecyclerViews();
        setupClickListeners();
        checkLocationPermission();
    }
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    private void performHapticFeedback() {
        View view = getCurrentFocus();
        if (view != null) {
            if (Build.VERSION.SDK_INT >= 33) { // TIRAMISU = 33
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } else if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= 33) { // TIRAMISU = 33
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
            } else if (Build.VERSION.SDK_INT >= 26) { // O = 26
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.EFFECT_CLICK));
            } else {
                vibrator.vibrate(20);
            }
        }
    }

    private void initViews() {
        // Toolbar
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        screenTitle = findViewById(R.id.screenTitle);
        
        // Saved locations section
        savedLocationsContainer = findViewById(R.id.savedLocationsContainer);
        myLocationCardView = findViewById(R.id.myLocationCardView);
        locationsRecyclerView = findViewById(R.id.locationsRecyclerView);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);
        savedLocationsTitle = findViewById(R.id.savedLocationsTitle);
        
        // Search section
        searchCardView = findViewById(R.id.searchCardView);
        searchEditText = findViewById(R.id.searchEditText);
        clearButton = findViewById(R.id.clearButton);
        searchResultsContainer = findViewById(R.id.searchResultsContainer);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        searchProgressBar = findViewById(R.id.searchProgressBar);
    }
    
    private void initData() {
        savedLocations = new ArrayList<>();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        repository = WeatherRepository.getInstance(this);
        
        if (Build.VERSION.SDK_INT >= 31) { // S = 31
            Object vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = ((VibratorManager) vibratorManager).getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        
        List<Location> loadedLocations = repository.loadSavedLocations();
        Log.d(TAG, "Loaded locations: " + loadedLocations.size());
        
        savedLocations.addAll(loadedLocations);
        updateEmptyState();
    }
    
    private void addDefaultLocation(String cityName) {
        repository.getLocationDetails(cityName, new WeatherRepository.LocationSearchCallback() {
            @Override
            public void onSearchResults(List<Location> locations) {
                if (!locations.isEmpty()) {
                    runOnUiThread(() -> {
                        savedLocations.addAll(locations);
                        locationsAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    });
                }
            }

            @Override
            public void onError(String message) {
                // mock data on error
                runOnUiThread(() -> {
                    savedLocations.add(new Location(
                            "328328",
                            "London",
                            "Partly Cloudy",
                            "15°",
                            false
                    ));
                    locationsAdapter.notifyDataSetChanged();
                    updateEmptyState();
                });
            }
        });
    }
    
    private void setupRecyclerViews() {
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                  @NonNull RecyclerView.ViewHolder viewHolder, 
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                if (fromPosition < 0 || toPosition < 0 || 
                    fromPosition >= savedLocations.size() || 
                    toPosition >= savedLocations.size()) {
                    return false;
                }
                
                // Move item in adapter - the adapter will handle preventing moves of the current location
                locationsAdapter.moveItem(fromPosition, toPosition);
                
                // Save the updated order to persistent storage
                repository.saveLocations(savedLocations);
                
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used for this implementation
            }
            
            @Override
            public boolean isLongPressDragEnabled() {
                // Disable long press drag as we're using the drag handle
                return false;
            }
            
            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        };
        
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(locationsRecyclerView);
        
        // Create and set adapter for saved locations
        locationsAdapter = new LocationsAdapter(this, savedLocations, this, itemTouchHelper);
        locationsRecyclerView.setAdapter(locationsAdapter);
        
        // Setup search results recycler view
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsAdapter = new SearchResultsAdapter(this, this);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
    }

    private void setupClickListeners() {
        // Navigation
        backButton.setOnClickListener(v -> {
            performHapticFeedback();
            onBackPressed();
        });
        
        // Toggle edit mode when edit button is clicked
        editButton.setOnClickListener(v -> {
            performHapticFeedback();
            toggleEditMode();
        });
        
        // My location card
        myLocationCardView.setOnClickListener(v -> {
            performHapticFeedback();
            // Only respond to clicks when not in edit mode
            if (!isEditMode) {
                if (checkLocationPermission()) {
                    // Show loading toast
                    Toast.makeText(this, "Fetching your current location...", Toast.LENGTH_SHORT).show();
                    
                    // Get current location
                    getCurrentLocation();
                }
            }
        });
        
        // Search functionality
        searchCardView.setOnClickListener(v -> {
            performHapticFeedback();
            if (!isSearchMode) {
                enterSearchMode();
            }
        });
        
        // Search text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on text
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                // If text is empty, hide search results
                if (s.length() == 0) {
                    searchResultsAdapter.clearResults();
                    showNoResults(false);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Auto-search after typing (with delay)
                if (s.length() >= 2) {
                    searchHandler.removeCallbacks(searchRunnable);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }
        });
        
        // Search action on keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                return true;
            }
            return false;
        });
        
        // Clear search button
        clearButton.setOnClickListener(v -> {
            performHapticFeedback();
            searchEditText.setText("");
            searchResultsAdapter.clearResults();
            showNoResults(false);
        });
    }
    
    /**
     * Toggle between edit and normal mode
     */
    private void toggleEditMode() {
        boolean wasInEditMode = isEditMode;
        isEditMode = !isEditMode;
        
        // Update UI for edit mode
        editButton.setImageResource(isEditMode ? R.drawable.ph_check : R.drawable.ph_pencil);
        editButton.setContentDescription(isEditMode ? "Done editing" : "Edit locations");
        
        // Update title text
        savedLocationsTitle.setText(isEditMode ? "Edit Locations" : "Saved Locations");
        
        // Update adapter to show/hide drag handles and delete buttons
        locationsAdapter.setEditMode(isEditMode);
        
        // If we're exiting edit mode, save the current order of locations
        if (wasInEditMode && !isEditMode) {
            Log.d(TAG, "Exiting edit mode, saving location order");
            repository.saveLocations(savedLocations);
        }
    }
    
    /**
     * Enter search mode - show search UI and focus on search field
     */
    private void enterSearchMode() {
        isSearchMode = true;
        
        // Update UI
        screenTitle.setText("Search Cities");
        editButton.setVisibility(View.GONE);
        searchEditText.requestFocus();
        
        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        
        // Show search results container
        searchResultsContainer.setVisibility(View.VISIBLE);
        savedLocationsContainer.setVisibility(View.GONE);
    }
    
    /**
     * Exit search mode - restore normal UI
     */
    private void exitSearchMode() {
        isSearchMode = false;
        
        // Update UI
        screenTitle.setText("Locations");
        editButton.setVisibility(View.VISIBLE);
        searchEditText.clearFocus();
        searchEditText.setText("");
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        
        // Show saved locations container
        searchResultsContainer.setVisibility(View.GONE);
        savedLocationsContainer.setVisibility(View.VISIBLE);
    }
    
    /**
     * Perform search for cities
     */
    private void performSearch(String query) {
        if (query.length() < 2) {
            return;
        }
        
        Log.d(TAG, "Performing search for: " + query);
        
        // Make sure we're in search mode
        if (!isSearchMode) {
            enterSearchMode();
        }
        
        // Show loading indicator
        showSearchLoading(true);
        
        // Clear previous results
        searchResultsAdapter.clearResults();
        
        // Search for cities using OpenWeatherMap API
        repository.searchLocationsByCity(query, new WeatherRepository.LocationSearchCallback() {
            @Override
            public void onSearchResults(List<Location> locations) {
                Log.d(TAG, "Search returned " + locations.size() + " results");
                
                runOnUiThread(() -> {
                    showSearchLoading(false);
                    
                    if (locations.isEmpty()) {
                        Log.d(TAG, "No results found for query: " + query);
                        showNoResults(true);
                        return;
                    }
                    
                    // Convert to search result items and check if already saved
                    List<SearchResultsAdapter.SearchResultItem> results = new ArrayList<>();
                    for (Location location : locations) {
                        boolean isSaved = isLocationSaved(location);
                        Log.d(TAG, "Adding result: " + location.getName() + ", saved=" + isSaved + ", temp=" + location.getTemperature() + ", condition=" + location.getCondition());
                        results.add(new SearchResultsAdapter.SearchResultItem(location, isSaved));
                    }
                    
                    // Update adapter with search results
                    searchResultsAdapter.setSearchResults(results);
                    showNoResults(false);
                });
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Search error: " + message);
                
                runOnUiThread(() -> {
                    showSearchLoading(false);
                    showNoResults(true);
                    Toast.makeText(LocationsActivity.this, "Error searching for '" + query + "': " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Show/hide loading indicator for search
     */
    private void showSearchLoading(boolean isLoading) {
        searchProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        searchResultsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
        
        // Make sure search results container is visible when searching
        if (searchResultsContainer.getVisibility() != View.VISIBLE) {
            searchResultsContainer.setVisibility(View.VISIBLE);
            savedLocationsContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show/hide no results message
     */
    private void showNoResults(boolean show) {
        noResultsText.setVisibility(show ? View.VISIBLE : View.GONE);
        searchResultsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Check if location is already saved
     */
    private boolean isLocationSaved(Location location) {
        for (Location savedLocation : savedLocations) {
            if (savedLocation.getId().equals(location.getId())) {
                return true;
            }
        }
        return false;
    }
    
    private void updateEmptyState() {
        if (savedLocations.isEmpty()) {
            emptyStateMessage.setVisibility(View.VISIBLE);
            locationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateMessage.setVisibility(View.GONE);
            locationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onLocationClick(Location location) {
        // Only respond to clicks when not in edit mode
        if (!isEditMode) {
            // Select this location and return to main screen
            Intent intent = new Intent();
            intent.putExtra("locationId", location.getId());
            intent.putExtra("locationName", location.getName());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    
    @Override
    public void onLocationDelete(Location location, int position) {
        // Remove the location from the list
        savedLocations.remove(position);
        locationsAdapter.notifyItemRemoved(position);
        updateEmptyState();
        
        // Save the updated list to storage
        repository.saveLocations(savedLocations);
    }
    
    @Override
    public void onResultClick(SearchResultsAdapter.SearchResultItem item) {
        // Handle click on search result
        if (item.isSaved()) {
            // If already saved, just return to main activity with this location
            Intent intent = new Intent();
            intent.putExtra("locationId", item.getLocation().getId());
            intent.putExtra("locationName", item.getLocation().getName());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // If not saved, add it first and finish the activity
            addLocation(item.getLocation(), true);
        }
    }
    
    @Override
    public void onAddClick(SearchResultsAdapter.SearchResultItem item) {
        // Add location to saved locations but don't finish the activity
        addLocation(item.getLocation(), false);
    }
    
    /**
     * Add a location to saved locations
     * @param location The location to add
     * @param finishActivity Whether to finish the activity after adding
     */
    private void addLocation(Location location, boolean finishActivity) {
        // Add to saved locations
        savedLocations.add(location);
        
        // Update the adapter to reflect the change
        if (locationsAdapter != null) {
            locationsAdapter.notifyDataSetChanged();
            updateEmptyState();
        }
        
        // Save to storage
        repository.saveLocations(savedLocations);
        
        // Log the saved locations for debugging
        Log.d(TAG, "Added location: " + location.getName() + ", ID: " + location.getId() + ", Temp: " + location.getTemperature());
        Log.d(TAG, "Total saved locations: " + savedLocations.size());
        
        // Show success message
        Toast.makeText(this, location.getName() + " added to saved locations", Toast.LENGTH_SHORT).show();
        
        // Update the search results to show the location is now saved
        if (searchResultsAdapter != null) {
            searchResultsAdapter.updateItemSavedStatus(location.getId(), true);
        }
        
        // If requested, return to main activity with this location
        if (finishActivity) {
            Intent intent = new Intent();
            intent.putExtra("locationId", location.getId());
            intent.putExtra("locationName", location.getName());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    
    /**
     * Check if we have location permission
     */
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }
    

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            
            myLocationCardView.setEnabled(false);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            if (location != null) {
                                String locationId = location.getLatitude() + "," + location.getLongitude();
                                
                                Log.d(TAG, "Got current location: " + locationId);
                                
                                String[] latLon = locationId.split(",");
                                if (latLon.length == 2) {
                                    try {
                                        double lat = Double.parseDouble(latLon[0]);
                                        double lon = Double.parseDouble(latLon[1]);
                                        
                                        Toast.makeText(LocationsActivity.this, 
                                                "Getting location details...", 
                                                Toast.LENGTH_SHORT).show();
                                        
                                        repository.getOpenWeatherService().getCurrentWeather(lat, lon, "metric", io.levs.weather.api.OpenWeatherService.API_KEY)
                                                .enqueue(new retrofit2.Callback<io.levs.weather.models.openweather.OpenWeatherCurrentResponse>() {
                                                    @Override
                                                    public void onResponse(retrofit2.Call<io.levs.weather.models.openweather.OpenWeatherCurrentResponse> call, 
                                                                           retrofit2.Response<io.levs.weather.models.openweather.OpenWeatherCurrentResponse> response) {
                                                        String locationName;
                                                        if (response.isSuccessful() && response.body() != null) {
                                                            String cityName = response.body().getName();
                                                            String countryCode = response.body().getSys() != null ? response.body().getSys().getCountry() : "";
                                                            
                                                            if (cityName != null && !cityName.isEmpty()) {
                                                                locationName = countryCode != null && !countryCode.isEmpty() ? 
                                                                        cityName + ", " + countryCode : cityName;
                                                            } else {
                                                                locationName = getString(R.string.current_location);
                                                            }
                                                        } else {
                                                            locationName = getString(R.string.current_location);
                                                        }
                                                        
                                                        // Return to main activity with this location
                                                        Intent intent = new Intent();
                                                        intent.putExtra("locationId", locationId);
                                                        intent.putExtra("locationName", locationName);
                                                        setResult(RESULT_OK, intent);
                                                        finish();
                                                    }
                                                    
                                                    @Override
                                                    public void onFailure(retrofit2.Call<io.levs.weather.models.openweather.OpenWeatherCurrentResponse> call, Throwable t) {
                                                        String locationName = getString(R.string.current_location);
                                                        
                                                        // Return to main activity with this location
                                                        Intent intent = new Intent();
                                                        intent.putExtra("locationId", locationId);
                                                        intent.putExtra("locationName", locationName);
                                                        setResult(RESULT_OK, intent);
                                                        finish();
                                                    }
                                                });
                                    } catch (NumberFormatException e) {
                                        // Fallback to default name
                                        String locationName = getString(R.string.current_location);
                                        
                                        // Return to main activity with this location
                                        Intent intent = new Intent();
                                        intent.putExtra("locationId", locationId);
                                        intent.putExtra("locationName", locationName);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                } else {
                                    // Fallback to default name
                                    String locationName = getString(R.string.current_location);
                                    
                                    // Return to main activity with this location
                                    Intent intent = new Intent();
                                    intent.putExtra("locationId", locationId);
                                    intent.putExtra("locationName", locationName);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            } else {
                                // Location was null, show error message
                                Toast.makeText(LocationsActivity.this, 
                                        "Could not get current location. Please try again.", 
                                        Toast.LENGTH_SHORT).show();
                                myLocationCardView.setEnabled(true);
                            }
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Log.e(TAG, "Error getting location", e);
                        Toast.makeText(LocationsActivity.this, 
                                "Error getting location: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        myLocationCardView.setEnabled(true);
                    });
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                
                // Try to get current location if this was from the my location card click
                if (myLocationCardView.isPressed()) {
                    getCurrentLocation();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied. Cannot use current location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        if (isSearchMode) {
            exitSearchMode();
        } else if (isEditMode) {
            toggleEditMode();
        } else {
            super.onBackPressed();
        }
    }
}
