package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.FollowApi;
import com.smartwater.monitoring.network.JwtInterceptor;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileActivity displays user profile information
 * ✅ Now loads profile from backend API and syncs to local cache
 */
public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBackProfile, btnLogout;
    private TextView tvProfileName, tvProfileEmail;
    private TextView tvProfileNameValue, tvProfileEmailValue, tvProfileContactValue;
    private Button btnEditProfile;

    // SharedPreferences for local data storage
    private SharedPreferences sharedPreferences;

    // ✅ Network API
    private FollowApi followApi;
    private TokenStore tokenStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // ✅ Initialize API client
        tokenStore = new TokenStore(this);
        JwtInterceptor.TokenProvider tokenProvider = () -> tokenStore.getToken();
        followApi = ApiClient.createFollow(this, tokenProvider);

        // Initialize UI components
        initializeViews();

        // ✅ Load profile from backend first, fallback to local
        loadProfileFromBackend();

        // Set click listeners
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning from update
        loadProfileFromBackend();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnBackProfile = findViewById(R.id.btnBackProfile);
        btnLogout = findViewById(R.id.btnLogout);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileNameValue = findViewById(R.id.tvProfileNameValue);
        tvProfileEmailValue = findViewById(R.id.tvProfileEmailValue);
        tvProfileContactValue = findViewById(R.id.tvProfileContactValue);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    /**
     * ✅ Load profile from backend API
     */
    private void loadProfileFromBackend() {
        followApi.getMyProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call,
                                   @NonNull Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse profile = response.body();
                    
                    // Update UI with backend data
                    String fullName = profile.getFullName();
                    if (fullName.isEmpty()) {
                        fullName = profile.getEmail() != null ? profile.getEmail().split("@")[0] : "User";
                    }
                    
                    String email = profile.getEmail() != null ? profile.getEmail() : "N/A";
                    String contact = profile.getContact() != null ? profile.getContact() : "N/A";
                    
                    tvProfileName.setText(fullName);
                    tvProfileEmail.setText(email);
                    tvProfileNameValue.setText(fullName);
                    tvProfileEmailValue.setText(email);
                    tvProfileContactValue.setText(contact);
                    
                    // ✅ Cache to local SharedPreferences for offline access
                    saveProfileToLocal(fullName, email, contact);
                    
                    android.util.Log.d("ProfileActivity", "✅ Loaded profile from backend: " + email);
                } else {
                    android.util.Log.e("ProfileActivity", "Failed to load profile: " + response.code());
                    // Fallback to local data
                    loadLocalProfile();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                android.util.Log.e("ProfileActivity", "API call failed: " + t.getMessage());
                // Fallback to local data
                loadLocalProfile();
            }
        });
    }

    /**
     * ✅ Save profile to local SharedPreferences for offline access
     */
    private void saveProfileToLocal(String name, String email, String contact) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", name);
        editor.putString("userEmail", email);
        editor.putString("userContact", contact);
        editor.apply();
    }

    /**
     * Load profile from local SharedPreferences (fallback)
     */
    private void loadLocalProfile() {
        String name = sharedPreferences.getString("userName", "User");
        String email = sharedPreferences.getString("userEmail", "email@example.com");
        String contact = sharedPreferences.getString("userContact", "N/A");

        // Update UI with local data
        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfileNameValue.setText(name);
        tvProfileEmailValue.setText(email);
        tvProfileContactValue.setText(contact);
        
        android.util.Log.d("ProfileActivity", "📱 Loaded profile from local cache");
    }

    /**
     * Set click listeners for buttons
     */
    private void setClickListeners() {
        // Back button click
        btnBackProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Logout button click
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        // Edit profile button click
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logoutUser();
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Logout user and navigate to login screen
     */
    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // ✅ Clear JWT token
        tokenStore.clear();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
