package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.JwtInterceptor;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.WaterApi;
import com.smartwater.monitoring.network.dto.SensorDataResponse;
import com.smartwater.monitoring.network.dto.WaterIngestRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import io.flutter.embedding.android.FlutterActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    // UI
    private ImageButton btnProfileIcon, btnLogoutIcon;
    private TextView tvQualityStatus, tvQualityDescription;
    private TextView tvQuickPh, tvQuickTemp, tvQuickStatus;
    private TextView tvCurrentPh, tvCurrentTemp; // Sensor Readings section
    private ProgressBar pbPhLevel, pbTempLevel; // Progress bars
    private TextView tabPh, tabTemperature;
    private ImageView ivQualityIcon;
    private LineChart phChart, temperatureChart;
    private ListView lvRecentSessions;
    private BottomNavigationView bottomNavigation;
    private View btnOpenFlutter;

    // ✅ Time range selector buttons
    private TextView btnRange1h, btnRange24h, btnRange7d, btnRange30d;
    private int selectedRangeHours = 1; // Default to 1 hour (1H) for live demo

    // Data
    private SharedPreferences sharedPreferences;
    private ArrayList<String> sessionList;
    private ArrayAdapter<String> sessionAdapter;

    private boolean ignoreFirstNavCallback = true;
    private boolean isOpeningFlutter = false;

    // ✅ Network
    private WaterApi waterApi;

    // ✅ Auto-refresh mechanism
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private static final int AUTO_REFRESH_INTERVAL_MS = 5000; // 5 seconds
    private boolean isAutoRefreshEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // ✅ init Retrofit (Spring as facade)
        TokenStore tokenStore = new TokenStore(this);
        JwtInterceptor.TokenProvider tokenProvider = () -> tokenStore.getToken();
        waterApi = ApiClient.createWater(this, tokenProvider);

        initializeViews();

        // ✅ Fetch all data from backend
        fetchAndRender();
        fetchChartData(); // ✅ Fetch real chart data from backend

        setClickListeners();
        setupBottomNavigation();
    }
    // ... (skip unchanged methods)

    private void initializeViews() {
        btnProfileIcon = findViewById(R.id.btnProfileIcon);
        btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        tvQualityStatus = findViewById(R.id.tvQualityStatus);
        tvQualityDescription = findViewById(R.id.tvQualityDescription);
        tvQuickPh = findViewById(R.id.tvQuickPh);
        tvQuickTemp = findViewById(R.id.tvQuickTemp);
        tvQuickStatus = findViewById(R.id.tvQuickStatus);

        // Sensor Readings section
        tvCurrentPh = findViewById(R.id.tvCurrentPh);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        pbPhLevel = findViewById(R.id.pbPhLevel);

        ivQualityIcon = findViewById(R.id.ivQualityIcon);
        phChart = findViewById(R.id.phChart);
        temperatureChart = findViewById(R.id.temperatureChart);
        lvRecentSessions = findViewById(R.id.lvRecentSessions);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        btnOpenFlutter = findViewById(R.id.btnOpenFlutter);
        tabPh = findViewById(R.id.tabPh);
        tabTemperature = findViewById(R.id.tabTemperature);

        // ✅ Initialize time range buttons
        btnRange1h = findViewById(R.id.btnRange1h);
        btnRange24h = findViewById(R.id.btnRange24h);
        btnRange7d = findViewById(R.id.btnRange7d);
        btnRange30d = findViewById(R.id.btnRange30d);

        // ✅ Default to 1H highlighted
        btnRange1h.setBackgroundResource(R.drawable.liquid_glass_tab);
        btnRange1h.setTextColor(Color.WHITE);
        btnRange24h.setBackground(null); // Clear 24h default
        btnRange24h.setTextColor(getResources().getColor(R.color.text_secondary_dark));

        sessionList = new ArrayList<>();
        sessionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessionList);
        lvRecentSessions.setAdapter(sessionAdapter);
    }

    private void setClickListeners() {
        btnProfileIcon.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
        });

        btnLogoutIcon.setOnClickListener(v -> showLogoutDialog());

        // ✅ Short click: Open Flutter
        btnOpenFlutter.setOnClickListener(v -> openFlutterDashboard());

        // ✅ Long click: Simulate Upload
        btnOpenFlutter.setOnLongClickListener(v -> {
            simulateUploadToSpringIngest();
            return true;
        });

        // ✅ Tab switching for charts
        tabPh.setOnClickListener(v -> selectTab(true));
        tabTemperature.setOnClickListener(v -> selectTab(false));

        // ✅ Time range switching
        btnRange1h.setOnClickListener(v -> selectTimeRange(1));
        btnRange24h.setOnClickListener(v -> selectTimeRange(24));
        btnRange7d.setOnClickListener(v -> selectTimeRange(24 * 7));
        btnRange30d.setOnClickListener(v -> selectTimeRange(24 * 30));
    }

    /**
     * ✅ Select time range and refresh chart
     */
    private void selectTimeRange(int hours) {
        selectedRangeHours = hours;
        updateTimeRangeButtonStyles();
        fetchChartData();
    }

    /**
     * ✅ Update button styles based on selected time range
     */
    private void updateTimeRangeButtonStyles() {
        // Clear all backgrounds
        btnRange1h.setBackground(null);
        btnRange24h.setBackground(null);
        btnRange7d.setBackground(null);
        btnRange30d.setBackground(null);

        int unselectedColor = getResources().getColor(R.color.text_secondary_dark);
        btnRange1h.setTextColor(unselectedColor);
        btnRange24h.setTextColor(unselectedColor);
        btnRange7d.setTextColor(unselectedColor);
        btnRange30d.setTextColor(unselectedColor);

        // Highlight selected button
        TextView selected;
        if (selectedRangeHours == 1) {
            selected = btnRange1h;
        } else if (selectedRangeHours == 24) {
            selected = btnRange24h;
        } else if (selectedRangeHours == 24 * 7) {
            selected = btnRange7d;
        } else {
            selected = btnRange30d;
        }

        selected.setBackgroundResource(R.drawable.liquid_glass_tab);
        selected.setTextColor(Color.WHITE);
    }

    private void selectTab(boolean showPh) {
        if (showPh) {
            phChart.setVisibility(View.VISIBLE);
            temperatureChart.setVisibility(View.GONE);
            tabPh.setBackgroundResource(R.drawable.liquid_glass_tab);
            tabPh.setTextColor(Color.WHITE);
            tabTemperature.setBackground(null);
            tabTemperature.setTextColor(getResources().getColor(R.color.text_secondary_dark));
        } else {
            phChart.setVisibility(View.GONE);
            temperatureChart.setVisibility(View.VISIBLE);
            tabTemperature.setBackgroundResource(R.drawable.liquid_glass_tab);
            tabTemperature.setTextColor(Color.WHITE);
            tabPh.setBackground(null);
            tabPh.setTextColor(getResources().getColor(R.color.text_secondary_dark));
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", null)
                .show();
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Clear token
        new TokenStore(this).clear();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openFlutterDashboard() {
        if (isOpeningFlutter)
            return;
        isOpeningFlutter = true;

        startActivity(
                FlutterActivity
                        .withNewEngine()
                        .initialRoute("/dashboard")
                        .build(DashboardActivity.this));

        new Handler(Looper.getMainLooper()).postDelayed(() -> isOpeningFlutter = false, 600);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {

            if (ignoreFirstNavCallback) {
                ignoreFirstNavCallback = false;
                return true;
            }

            int itemId = item.getItemId();

            if (itemId == R.id.navigation_dashboard) {
                return true;
            } else if (itemId == R.id.navigation_bluetooth) {
                startActivity(new Intent(DashboardActivity.this, BluetoothActivity.class));
                return true;
            } else if (itemId == R.id.navigation_alerts) {
                startActivity(new Intent(DashboardActivity.this, AlertActivity.class));
                return true;
            } else if (itemId == R.id.navigation_reports) {
                startActivity(new Intent(DashboardActivity.this, ReportActivity.class));
                return true;
            } else if (itemId == R.id.navigation_community) {
                startActivity(new Intent(DashboardActivity.this, CommunityActivity.class));
                return true;
            }

            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    /**
     * ✅ Upload sample data to backend
     */
    private void simulateUploadToSpringIngest() {
        Random r = new Random();

        double ph = 6.0 + (r.nextDouble() * 3.5);
        double temp = 20.0 + (r.nextDouble() * 15.0);
        double turb = 0.5 + (r.nextDouble() * 6.0);
        String[] locations = { "UM Lake", "KK3", "Lake A", "Lake B" };
        String location = locations[r.nextInt(locations.length)];

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                .format(new Date());

        WaterIngestRequest req = new WaterIngestRequest(ph, temp, turb, location, timestamp);

        Toast.makeText(this, "Uploading sample reading...", Toast.LENGTH_SHORT).show();

        waterApi.ingest(req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                    @NonNull Response<Map<String, Object>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this,
                            "Ingest failed: HTTP " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(DashboardActivity.this,
                        "✅ Uploaded! Refreshing dashboard...",
                        Toast.LENGTH_SHORT).show();

                // Refresh all data
                fetchAndRender();
                fetchChartData();
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(DashboardActivity.this,
                        "Ingest error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * ✅ Fetch latest reading and summary from backend
     */
    private void fetchAndRender() {
        // 1) latest - Use "WATER_001" directly for live demo data
        waterApi.getDeviceLatest("WATER_001").enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                    @NonNull Response<Map<String, Object>> response) {

                if (!response.isSuccessful()) {
                    tvQualityStatus.setText("NO DATA");
                    tvQualityStatus.setTextColor(Color.GRAY);
                    tvQualityDescription.setText("No live data for WATER_001.");
                    tvQuickPh.setText("—");
                    tvQuickTemp.setText("—");
                    tvQuickStatus.setText("—");
                    tvQuickStatus.setTextColor(Color.GRAY);
                    tvCurrentPh.setText("—");
                    tvCurrentTemp.setText("—");
                    ivQualityIcon.setColorFilter(Color.GRAY);
                    return;
                }

                Map<String, Object> body = response.body();
                if (body == null)
                    return;

                Object phObj = body.get("ph");
                Object tempObj = body.get("temperature");

                double ph = phObj instanceof Number ? ((Number) phObj).doubleValue() : 7.0;
                double temp = tempObj instanceof Number ? ((Number) tempObj).doubleValue() : 25.0;

                // Update quick stats header
                tvQuickPh.setText(String.format(Locale.US, "%.2f", ph));
                tvQuickTemp.setText(String.format(Locale.US, "%.1f°C", temp));

                // Update Sensor Readings section
                tvCurrentPh.setText(String.format(Locale.US, "%.2f", ph));
                tvCurrentTemp.setText(String.format(Locale.US, "%.1f°C", temp));

                // Set colors based on values
                int phColor = (ph >= 6.5 && ph <= 8.5) ? Color.parseColor("#4CAF50")
                        : ((ph >= 6.0 && ph < 6.5) || (ph > 8.5 && ph <= 9.0)) ? Color.parseColor("#FF9800")
                                : Color.parseColor("#F44336");
                tvCurrentPh.setTextColor(phColor);

                int tempColor = (temp >= 20 && temp <= 30) ? Color.parseColor("#4CAF50")
                        : ((temp >= 15 && temp < 20) || (temp > 30 && temp <= 35)) ? Color.parseColor("#FF9800")
                                : Color.parseColor("#F44336");
                tvCurrentTemp.setTextColor(tempColor);

                // Update progress bars (pH scale 0-14, temp scale 0-50)
                if (pbPhLevel != null)
                    pbPhLevel.setProgress((int) (ph / 14.0 * 100));
                if (pbTempLevel != null)
                    pbTempLevel.setProgress((int) (temp / 50.0 * 100));

                applyStatusFromPh(ph);

                // ✅ LIVE CHART UPDATE: Append new data point directly
                addEntryToChart(phChart, (float) ph);
                addEntryToChart(temperatureChart, (float) temp);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                tvQualityStatus.setText("OFFLINE");
                tvQualityStatus.setTextColor(Color.GRAY);
                tvQualityDescription.setText("Cannot reach server: " + t.getMessage());
                ivQualityIcon.setColorFilter(Color.GRAY);
            }
        });

        // 2) summary - 需要传入时间范围参数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());

        Calendar cal = Calendar.getInstance();
        String toTime = sdf.format(cal.getTime());

        cal.add(Calendar.HOUR, -24);
        String fromTime = sdf.format(cal.getTime());

        waterApi.getMySummary(fromTime, toTime).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                    @NonNull Response<Map<String, Object>> response) {

                if (!response.isSuccessful())
                    return;

                Map<String, Object> body = response.body();
                if (body == null)
                    return;

                Object totalRecords = body.get("totalRecords");
                Object overallStatus = body.get("overallStatus");

                String total = totalRecords == null ? "0" : String.valueOf(totalRecords);
                String overall = overallStatus == null ? "NO_DATA" : String.valueOf(overallStatus);

                tvQualityDescription.setText("Summary (24h): " + total + " records, " + overall);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                // ignore
            }
        });
    }

    /**
     * ✅ Helper to add entry to chart dynamically
     */
    private void addEntryToChart(LineChart chart, float value) {
        if (chart == null)
            return;

        LineData data = chart.getData();
        if (data == null || data.getEntryCount() == 0) { // Check for empty data too
            // If explicit data not set yet, create a dummy set
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(0, value));
            LineDataSet set = new LineDataSet(entries, "Data");
            // Basic styling - usually configureChart does better
            set.setLineWidth(2f);
            set.setDrawCircles(false); // smoother look
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setDrawFilled(true);

            data = new LineData(set);
            chart.setData(data);
        } else {
            // Append
            com.github.mikephil.charting.interfaces.datasets.ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = new LineDataSet(new ArrayList<>(), "Data"); // Should not happen if data != null
                data.addDataSet(set);
            }

            // X-index = count
            float x = set.getEntryCount();
            data.addEntry(new Entry(x, value), 0);

            data.notifyDataChanged();
            chart.notifyDataSetChanged();

            // Limit view to 100 points to keep it "moving"
            chart.setVisibleXRangeMaximum(50);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    /**
     * ✅ Fetch chart data from backend (NOT HARDCODED!)
     */
    private void fetchChartData() {
        // ✅ Calculate time range based on selected hours
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());

        Calendar cal = Calendar.getInstance();
        String to = sdf.format(cal.getTime());

        cal.add(Calendar.HOUR, -selectedRangeHours);
        String from = sdf.format(cal.getTime());

        Toast.makeText(this, "Loading chart history...", Toast.LENGTH_SHORT).show();

        // ✅ Use getDeviceHistory with "WATER_001" for live chart data
        waterApi.getDeviceHistory("WATER_001", from, to).enqueue(new Callback<List<SensorDataResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<SensorDataResponse>> call,
                    @NonNull Response<List<SensorDataResponse>> response) {

                ArrayList<Entry> phEntries = new ArrayList<>();
                ArrayList<Entry> tempEntries = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<SensorDataResponse> data = response.body();

                    // Populate chart entries from backend data
                    for (int i = 0; i < data.size(); i++) {
                        SensorDataResponse item = data.get(i);
                        // Make sure we have valid timestamps or indices
                        // For simplicity in this demo, using index i as X-axis
                        if (item.getPh() != null) {
                            phEntries.add(new Entry(i, item.getPh().floatValue()));
                        }
                        if (item.getTemperature() != null) {
                            tempEntries.add(new Entry(i, item.getTemperature().floatValue()));
                        }
                    }

                    // Update sessions list from backend data
                    updateSessionsFromBackend(data);
                }

                // If no data from backend, start empty
                if (phEntries.isEmpty()) {
                    phEntries.add(new Entry(0, 7f)); // Default baseline
                }
                if (tempEntries.isEmpty()) {
                    tempEntries.add(new Entry(0, 25f)); // Default baseline
                }

                configureChart(phChart, phEntries, "pH Level", Color.parseColor("#4CAF50"));
                configureChart(temperatureChart, tempEntries, "Temperature (°C)", Color.parseColor("#FF9800"));
            }

            @Override
            public void onFailure(@NonNull Call<List<SensorDataResponse>> call, @NonNull Throwable t) {
                // ...
            }
        });
    }

    /**
     * ✅ Update sessions list from backend data (NOT HARDCODED!)
     */
    private void updateSessionsFromBackend(List<SensorDataResponse> data) {
        sessionList.clear();

        int count = Math.min(data.size(), 5);
        for (int i = data.size() - count; i < data.size(); i++) {
            SensorDataResponse item = data.get(i);

            String timestamp = item.getTimestamp() != null ? item.getTimestamp() : "Unknown";
            String location = item.getLocation() != null ? item.getLocation() : "Unknown Location";
            String ph = item.getPh() != null ? String.format(Locale.US, "%.2f", item.getPh()) : "—";
            String temp = item.getTemperature() != null ? String.format(Locale.US, "%.1f", item.getTemperature()) : "—";

            String sessionInfo = timestamp + "\n" +
                    "Location: " + location + "\n" +
                    "pH: " + ph + " | Temp: " + temp + "°C";

            sessionList.add(sessionInfo);
        }

        sessionAdapter.notifyDataSetChanged();
    }

    private void applyStatusFromPh(double ph) {
        String status;
        String quickStatus;
        String description;
        int color;

        // Three-tier water quality classification
        if (ph >= 6.5 && ph <= 8.5) {
            status = "SAFE ✓";
            quickStatus = "SAFE";
            description = "Water quality is excellent and safe for use";
            color = Color.parseColor("#4CAF50");
        } else if ((ph >= 6.0 && ph < 6.5) || (ph > 8.5 && ph <= 9.0)) {
            status = "MODERATE ⚠";
            quickStatus = "MODERATE";
            description = "Water quality requires monitoring and attention";
            color = Color.parseColor("#FF9800");
        } else {
            status = "POLLUTED ✗";
            quickStatus = "POLLUTED";
            description = "Water quality is unsafe - immediate action required!";
            color = Color.parseColor("#F44336");
        }

        tvQualityStatus.setText(status);
        tvQualityStatus.setTextColor(color);
        tvQualityDescription.setText(description);
        ivQualityIcon.setColorFilter(color);

        // ✅ Also update quick status card
        tvQuickStatus.setText(quickStatus);
        tvQuickStatus.setTextColor(color);
    }

    private void configureChart(LineChart chart, ArrayList<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setBackgroundColor(Color.TRANSPARENT);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE); // White text for dark background

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#30FFFFFF"));
        leftAxis.setTextColor(Color.WHITE); // White text for dark background
        chart.getAxisRight().setEnabled(false);

        // ✅ Zoom to end initially if many points
        if (entries.size() > 50) {
            chart.setVisibleXRangeMaximum(50);
            chart.moveViewToX(entries.size());
        }

        chart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAndRender();
        fetchChartData(); // ✅ Fetch full history once on resume
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    /**
     * ✅ Start auto-refresh timer for real-time Dashboard updates
     */
    private void startAutoRefresh() {
        if (!isAutoRefreshEnabled)
            return;

        if (autoRefreshHandler == null) {
            autoRefreshHandler = new Handler(Looper.getMainLooper());
        }

        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isAutoRefreshEnabled) {
                        android.util.Log.d("Dashboard", "♻️ Auto-refreshing data...");
                        fetchAndRender();
                        // fetchChartData(); // ❌ REMOVED: Do not re-fetch history, we append now
                        autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL_MS);
                    }
                }
            };
        }

        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL_MS);
        android.util.Log.d("Dashboard", "✅ Auto-refresh started (every " + AUTO_REFRESH_INTERVAL_MS / 1000 + "s)");
    }

    /**
     * ✅ Stop auto-refresh timer
     */
    private void stopAutoRefresh() {
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
            android.util.Log.d("Dashboard", "⏸️ Auto-refresh stopped");
        }
    }
}
