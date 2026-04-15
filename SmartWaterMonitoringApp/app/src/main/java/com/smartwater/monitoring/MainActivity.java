package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity serves as the main navigation hub with bottom navigation bar
 * Provides access to all 5 modules of the SmartWater Monitoring App
 */
public class MainActivity extends AppCompatActivity {

    // UI Components
    private TextView tvWelcome;
    private CardView cvProfile;
    private BottomNavigationView bottomNavigation;

    // SharedPreferences
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Load user data
        loadUserData();

        // Set click listeners
        setClickListeners();

        // Set bottom navigation listener
        setupBottomNavigation();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cvProfile = findViewById(R.id.cvProfile);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    /**
     * Load user data and display welcome message
     */
    private void loadUserData() {
        String userName = sharedPreferences.getString("userName", "User");
        tvWelcome.setText("Welcome, " + userName + "!");
    }

    /**
     * Set click listeners for cards
     */
    private void setClickListeners() {
        // Profile
        cvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Setup bottom navigation bar
     */
    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_dashboard) {
                    // Already on main/dashboard - do nothing or refresh
                    return true;

                } else if (itemId == R.id.navigation_bluetooth) {
                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                    startActivity(intent);
                    return true;

                } else if (itemId == R.id.navigation_alerts) {
                    Intent intent = new Intent(MainActivity.this, AlertActivity.class);
                    startActivity(intent);
                    return true;

                } else if (itemId == R.id.navigation_reports) {
                    Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                    startActivity(intent);
                    return true;

                } else if (itemId == R.id.navigation_community) {
                    Intent intent = new Intent(MainActivity.this, CommunityActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });

        // Set Dashboard as selected by default
        bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning to main activity
        loadUserData();

        // Keep dashboard selected when returning
        bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
    }
}
