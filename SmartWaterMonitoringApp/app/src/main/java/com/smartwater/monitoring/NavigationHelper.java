package com.smartwater.monitoring;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * NavigationHelper - Reusable navigation setup for all activities
 * Handles top bar (profile & logout) and bottom navigation
 */
public class NavigationHelper {

    private Activity activity;
    private SharedPreferences sharedPreferences;

    public NavigationHelper(Activity activity) {
        this.activity = activity;
        this.sharedPreferences = activity.getSharedPreferences("SmartWaterPrefs", Activity.MODE_PRIVATE);
    }

    /**
     * Setup top bar with profile and logout icons
     */
    public void setupTopBar(ImageButton btnProfile, ImageButton btnLogout) {
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    /**
     * Setup bottom navigation bar
     * @param bottomNav - BottomNavigationView instance
     * @param currentItemId - ID of the current screen to highlight (e.g., R.id.navigation_dashboard)
     */
    public void setupBottomNavigation(BottomNavigationView bottomNav, int currentItemId) {
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_dashboard) {
                    navigateTo(DashboardActivity.class);
                    return true;
                } else if (itemId == R.id.navigation_bluetooth) {
                    navigateTo(BluetoothActivity.class);
                    return true;
                } else if (itemId == R.id.navigation_alerts) {
                    navigateTo(AlertActivity.class);
                    return true;
                } else if (itemId == R.id.navigation_reports) {
                    navigateTo(ReportActivity.class);
                    return true;
                } else if (itemId == R.id.navigation_community) {
                    navigateTo(CommunityActivity.class);
                    return true;
                }

                return false;
            }
        });

        // Set current item as selected
        bottomNav.setSelectedItemId(currentItemId);
    }

    /**
     * Navigate to another activity
     */
    private void navigateTo(Class<?> targetActivity) {
        // Don't navigate if already on that screen
        if (!activity.getClass().equals(targetActivity)) {
            Intent intent = new Intent(activity, targetActivity);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Logout user and return to login screen
     */
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
