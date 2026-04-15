package com.smartwater.monitoring;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.JwtInterceptor;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.WaterApi;
import com.smartwater.monitoring.network.dto.SensorDataResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map; // ✅ Added Map import
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AlertActivity displays water quality alerts based on threshold violations
 * ✅ Now generates alerts dynamically from "WATER_001" live data
 */
public class AlertActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnProfileIcon, btnLogoutIcon;
    private Button btnFilterAll, btnFilterCritical, btnFilterWarning, btnFilterInfo;
    private ListView lvAlerts;
    private BottomNavigationView bottomNavigation;

    // Navigation Helper
    private NavigationHelper navigationHelper;

    // Data
    private ArrayList<Alert> alertList;
    private ArrayList<Alert> filteredAlertList;
    private AlertAdapter alertAdapter;
    private String currentFilter = "All";

    // ✅ Network API
    private WaterApi waterApi;

    // ✅ Auto-refresh mechanism
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private static final int AUTO_REFRESH_INTERVAL_MS = 5000; // 5 seconds
    private boolean isAutoRefreshEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        navigationHelper = new NavigationHelper(this); // Helper for nav

        // ✅ Initialize API client (WaterApi)
        TokenStore tokenStore = new TokenStore(this);
        JwtInterceptor.TokenProvider tokenProvider = () -> tokenStore.getToken();
        waterApi = ApiClient.createWater(this, tokenProvider);

        // Initialize views
        initializeViews();

        // ✅ Initial fetch
        fetchLatestAlert();

        // Set click listeners
        setClickListeners();

        // Setup navigation logic
        setupNavigationAndListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLatestAlert(); // ✅ Initial fetch
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnProfileIcon = findViewById(R.id.btnProfileIcon);
        btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterCritical = findViewById(R.id.btnFilterCritical);
        btnFilterWarning = findViewById(R.id.btnFilterWarning);
        btnFilterInfo = findViewById(R.id.btnFilterInfo);
        lvAlerts = findViewById(R.id.lvAlerts);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        alertList = new ArrayList<>();
        filteredAlertList = new ArrayList<>();
        alertAdapter = new AlertAdapter(this, filteredAlertList);
        lvAlerts.setAdapter(alertAdapter);
    }

    /**
     * Set click listeners for filter buttons
     */
    private void setClickListeners() {
        btnFilterAll.setOnClickListener(v -> filterAlerts("All"));
        btnFilterCritical.setOnClickListener(v -> filterAlerts("Critical"));
        btnFilterWarning.setOnClickListener(v -> filterAlerts("Warning"));
        btnFilterInfo.setOnClickListener(v -> filterAlerts("Info"));
        
        btnProfileIcon.setOnClickListener(v -> startActivity(new android.content.Intent(this, ProfileActivity.class)));
        btnLogoutIcon.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupNavigationAndListeners() {
         bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                startActivity(new android.content.Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.navigation_bluetooth) {
                startActivity(new android.content.Intent(this, BluetoothActivity.class));
                return true;
            } else if (itemId == R.id.navigation_alerts) {
                return true;
            } else if (itemId == R.id.navigation_reports) {
                startActivity(new android.content.Intent(this, ReportActivity.class));
                return true;
            } else if (itemId == R.id.navigation_community) {
                startActivity(new android.content.Intent(this, CommunityActivity.class));
                return true;
            }
            return false;
        });
        bottomNavigation.setSelectedItemId(R.id.navigation_alerts);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                     new TokenStore(this).clear();
                     android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                     intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     startActivity(intent);
                     finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ✅ Track last seen data to avoid duplicates
    private String lastProcessedTimestamp = "";

    /**
     * ✅ Fetch ONLY the latest data point and add to top of list
     */
    private void fetchLatestAlert() {
        waterApi.getDeviceLatest("WATER_001").enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    
                    // Extract fields
                    Object tsObj = data.get("timestamp");
                    String timestamp = (tsObj != null) ? tsObj.toString() : "";
                    
                    // ✅ De-duplication: Skip if we already processed this timestamp
                    if (timestamp.equals(lastProcessedTimestamp)) {
                        return; 
                    }
                    lastProcessedTimestamp = timestamp;

                    Double ph = null; 
                    Double temp = null;
                    
                    if (data.get("ph") instanceof Number) {
                        ph = ((Number) data.get("ph")).doubleValue();
                    }
                    if (data.get("temperature") instanceof Number) {
                        temp = ((Number) data.get("temperature")).doubleValue();
                    }

                    if (ph != null && temp != null) {
                        Alert alert = generateSingleAlert(ph, temp, timestamp);
                        
                        // ✅ Prepend to top of list (Feed style)
                        alertList.add(0, alert);
                        
                        // Keep list size manageable
                        if (alertList.size() > 100) {
                            alertList.remove(alertList.size() - 1);
                        }
                        
                        filterAlerts(currentFilter);
                        android.util.Log.d("AlertActivity", "✅ New Alert Added: " + timestamp);
                    }

                } else {
                     android.util.Log.e("AlertActivity", "Fetch Latest Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                 android.util.Log.e("AlertActivity", "Network Error: " + t.getMessage());
            }
        });
    }

    /**
     * ✅ Generate a single Alert object based on values
     */
    private Alert generateSingleAlert(double ph, double temp, String timestamp) {
        // CRITICAL: pH < 6.0 or pH > 9.0
        if (ph < 6.0 || ph > 9.0) {
            return new Alert("Critical", "Critical pH Level", 
                    "Water is highly " + (ph < 6.0 ? "acidic" : "alkaline"), 
                    String.format(Locale.US, "pH: %.2f | Temp: %.1f°C", ph, temp), timestamp);
        }
        
        // WARNING: pH 6.0-6.5 or 8.5-9.0
        if (ph < 6.5 || ph > 8.5) {
             return new Alert("Warning", "Abnormal pH Level", 
                    "pH level deviating from optimal range", 
                    String.format(Locale.US, "pH: %.2f | Temp: %.1f°C", ph, temp), timestamp);
        }
        
        // WARNING: Temp > 30.0
        if (temp > 30.0) {
             return new Alert("Warning", "High Temperature", 
                    "Water temperature is elevated", 
                    String.format(Locale.US, "Temp: %.1f°C | pH: %.2f", temp, ph), timestamp);
        }

        // INFO (Safe)
        return new Alert("Info", "Normal Reading", 
                "Water quality is within safe limits", 
                String.format(Locale.US, "pH: %.2f | Temp: %.1f°C", ph, temp), timestamp);
    }

    /**
     * Filter alerts by type
     */
    private void filterAlerts(String filter) {
        currentFilter = filter;
        filteredAlertList.clear();

        for (Alert alert : alertList) {
            if (filter.equals("All") || alert.type.equals(filter)) {
                filteredAlertList.add(alert);
            }
        }
        alertAdapter.notifyDataSetChanged();
        updateFilterButtonStyles(filter);
    }

    private void updateFilterButtonStyles(String filter) {
        resetFilterButtons();
        switch (filter) {
            case "All":
                btnFilterAll.setBackgroundColor(Color.parseColor("#2196F3"));
                btnFilterAll.setTextColor(Color.WHITE);
                break;
            case "Critical":
                btnFilterCritical.setBackgroundColor(Color.parseColor("#F44336"));
                btnFilterCritical.setTextColor(Color.WHITE);
                break;
            case "Warning":
                btnFilterWarning.setBackgroundColor(Color.parseColor("#FF9800"));
                btnFilterWarning.setTextColor(Color.WHITE);
                break;
            case "Info":
                btnFilterInfo.setBackgroundColor(Color.parseColor("#4CAF50"));
                btnFilterInfo.setTextColor(Color.WHITE);
                break;
        }
    }

    private void resetFilterButtons() {
        int defaultColor = Color.parseColor("#E0E0E0");
        int textColor = Color.parseColor("#212121");
        btnFilterAll.setBackgroundColor(defaultColor);
        btnFilterAll.setTextColor(textColor);
        btnFilterCritical.setBackgroundColor(defaultColor);
        btnFilterCritical.setTextColor(textColor);
        btnFilterWarning.setBackgroundColor(defaultColor);
        btnFilterWarning.setTextColor(textColor);
        btnFilterInfo.setBackgroundColor(defaultColor);
        btnFilterInfo.setTextColor(textColor);
    }
    
    private void startAutoRefresh() {
        if (!isAutoRefreshEnabled) return;
        if (autoRefreshHandler == null) autoRefreshHandler = new Handler(Looper.getMainLooper());
        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isAutoRefreshEnabled) {
                        fetchLatestAlert(); // ✅ Incremental update
                        autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL_MS);
                    }
                }
            };
        }
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL_MS);
    }

    private void stopAutoRefresh() {
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    private static class Alert {
        String type;
        String title;
        String message;
        String threshold;
        String timestamp;

        Alert(String type, String title, String message, String threshold, String timestamp) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.threshold = threshold;
            this.timestamp = timestamp;
        }
    }

    private class AlertAdapter extends ArrayAdapter<Alert> {
        AlertAdapter(Context context, ArrayList<Alert> alerts) {
            super(context, 0, alerts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Alert alert = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_alert, parent, false);
            }

            ImageView ivAlertIcon = convertView.findViewById(R.id.ivAlertIcon);
            TextView tvAlertType = convertView.findViewById(R.id.tvAlertType);
            TextView tvAlertTime = convertView.findViewById(R.id.tvAlertTime);
            TextView tvAlertTitle = convertView.findViewById(R.id.tvAlertTitle);
            TextView tvAlertMessage = convertView.findViewById(R.id.tvAlertMessage);
            TextView tvAlertThreshold = convertView.findViewById(R.id.tvAlertThreshold);

            if (alert != null) {
                tvAlertType.setText(alert.type);
                tvAlertTime.setText(formatTime(alert.timestamp));
                tvAlertTitle.setText(alert.title);
                tvAlertMessage.setText(alert.message);
                tvAlertThreshold.setText(alert.threshold);

                int color;
                switch (alert.type) {
                    case "Critical": color = Color.parseColor("#F44336"); break;
                    case "Warning": color = Color.parseColor("#FF9800"); break;
                    default: color = Color.parseColor("#4CAF50"); break;
                }
                ivAlertIcon.setColorFilter(color);
                tvAlertType.setBackgroundColor(color);
            }
            return convertView;
        }

        private String formatTime(String t) {
            if (t == null) return "";
            if (t.contains("T")) return t.substring(11, Math.min(19, t.length()));
            return t;
        }
    }
}
