package com.smartwater.monitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.ReportApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.PollutionReportResponse;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * ReportActivity displays list of submitted pollution reports
 */
public class ReportActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnProfileIcon, btnLogoutIcon;
    private Button btnAddReport;
    private ListView lvReports;
    private BottomNavigationView bottomNavigation;

    // Navigation Helper
    private NavigationHelper navigationHelper;

    // Data
    private ArrayList<Report> reportList;
    private ReportAdapter reportAdapter;
    private SharedPreferences sharedPreferences;

    // Backend API
    private ReportApi reportApi;
    private TokenStore tokenStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize navigation helper
        navigationHelper = new NavigationHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // Initialize Backend API
        tokenStore = new TokenStore(this);
        reportApi = ApiClient.createReport(this, () -> tokenStore.getToken());

        // Initialize views
        initializeViews();

        // Load reports from backend
        loadReportsFromBackend();

        //Set click listeners
        setClickListeners();

        // Setup navigation
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload reports when returning from submit screen
        loadReportsFromBackend();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnProfileIcon = findViewById(R.id.btnProfileIcon);
        btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        btnAddReport = findViewById(R.id.btnAddReport);
        lvReports = findViewById(R.id.lvReports);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        lvReports.setAdapter(reportAdapter);
    }

    /**
     * Set click listeners
     */
    private void setClickListeners() {
        btnAddReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportActivity.this, SubmitReportActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Setup navigation
     */
    private void setupNavigation() {
        navigationHelper.setupTopBar(btnProfileIcon, btnLogoutIcon);
        navigationHelper.setupBottomNavigation(bottomNavigation, R.id.navigation_reports);
    }

    /**
     * Load reports from backend API
     */
    private void loadReportsFromBackend() {
        reportApi.getMyReports().enqueue(new Callback<List<PollutionReportResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<PollutionReportResponse>> call,
                                 @NonNull Response<List<PollutionReportResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ReportActivity.this,
                            "Failed to load reports: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                reportList.clear();
                for (PollutionReportResponse reportResp : response.body()) {
                    Report report = new Report(
                            reportResp.getLocation() != null ? reportResp.getLocation() : "Unknown Location",
                            reportResp.getDescription(),
                            reportResp.getLocation() != null ? reportResp.getLocation() : "Unknown",
                            reportResp.getCreatedAt() != null ? reportResp.getCreatedAt() : "Just now",
                            reportResp.getStatus() != null ? reportResp.getStatus() : "PENDING"
                    );
                    reportList.add(report);
                }

                if (reportList.isEmpty()) {
                    Toast.makeText(ReportActivity.this, 
                            "No reports yet. Click + to submit a report.", 
                            Toast.LENGTH_LONG).show();
                }

                reportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<PollutionReportResponse>> call,
                                @NonNull Throwable t) {
                Toast.makeText(ReportActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Load reports from SharedPreferences (deprecated)
     */
    private void loadReports() {
        reportList.clear();

        try {
            String reportsJson = sharedPreferences.getString("reports", "[]");
            JSONArray jsonArray = new JSONArray(reportsJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Report report = new Report(
                        jsonObject.getString("title"),
                        jsonObject.getString("description"),
                        jsonObject.getString("location"),
                        jsonObject.getString("timestamp"),
                        jsonObject.getString("status")
                );
                reportList.add(report);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (reportList.isEmpty()) {
            Toast.makeText(this, "No reports yet. Click + to submit a report.", Toast.LENGTH_LONG).show();
        }

        reportAdapter.notifyDataSetChanged();
    }

    /**
     * Report data class
     */
    private static class Report {
        String title;
        String description;
        String location;
        String timestamp;
        String status;

        Report(String title, String description, String location, String timestamp, String status) {
            this.title = title;
            this.description = description;
            this.location = location;
            this.timestamp = timestamp;
            this.status = status;
        }
    }

    /**
     * Custom adapter for displaying reports
     */
    private class ReportAdapter extends ArrayAdapter<Report> {
        ReportAdapter(Context context, ArrayList<Report> reports) {
            super(context, 0, reports);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Report report = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_report, parent, false);
            }

            TextView tvReportTitle = convertView.findViewById(R.id.tvReportTitle);
            TextView tvReportTime = convertView.findViewById(R.id.tvReportTime);
            TextView tvReportDescription = convertView.findViewById(R.id.tvReportDescription);
            TextView tvReportStatus = convertView.findViewById(R.id.tvReportStatus);

            if (report != null) {
                tvReportTitle.setText(report.title);
                tvReportTime.setText(report.timestamp);
                tvReportDescription.setText(report.description);
                tvReportStatus.setText("Status: " + report.status);
            }

            return convertView;
        }
    }
}
