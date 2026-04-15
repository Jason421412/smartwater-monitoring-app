package com.smartwater.monitoring;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.List;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.ReportApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.CreateReportRequest;
import com.smartwater.monitoring.network.dto.PollutionReportResponse;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SubmitReportActivity allows users to submit pollution reports
 * Includes title, description, location, and optional photo attachment
 */
public class SubmitReportActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBackSubmitReport;
    private TextInputLayout tilReportLocation;
    private TextInputEditText etReportTitle, etReportDescription, etReportLocation;
    private Button btnSelectPhoto, btnSubmitReport;
    private TextView tvPhotoStatus;
    private ImageView ivPhotoPreview;

    // Data
    private SharedPreferences sharedPreferences;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String photoFileName = "";

    // Permission request code
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private static final int LOCATION_PERMISSION_CODE = 103;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> mapLauncher;

    // Backend API
    private ReportApi reportApi;
    private TokenStore tokenStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_submit_report);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // Initialize Backend API
        tokenStore = new TokenStore(this);
        reportApi = ApiClient.createReport(this, () -> tokenStore.getToken());

        // Initialize views
        initializeViews();

        // Initialize activity result launchers
        initializeActivityResultLaunchers();

        // Set click listeners
        setClickListeners();

        // Initialize location client and get auto location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getAutoLocation();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnBackSubmitReport = findViewById(R.id.btnBackSubmitReport);
        etReportTitle = findViewById(R.id.etReportTitle);
        etReportDescription = findViewById(R.id.etReportDescription);
        tilReportLocation = findViewById(R.id.tilReportLocation);
        etReportLocation = findViewById(R.id.etReportLocation);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        tvPhotoStatus = findViewById(R.id.tvPhotoStatus);
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
    }

    /**
     * Initialize Activity Result Launchers for Gallery and Camera
     */
    private void initializeActivityResultLaunchers() {
        // Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), selectedImageUri);

                            // Display preview
                            ivPhotoPreview.setImageBitmap(selectedImageBitmap);
                            ivPhotoPreview.setVisibility(View.VISIBLE);

                            photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";
                            tvPhotoStatus.setText("Photo selected: " + photoFileName);
                            tvPhotoStatus.setVisibility(View.VISIBLE);

                            Toast.makeText(this, "Photo selected successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        selectedImageBitmap = (Bitmap) extras.get("data");

                        // Display preview
                        ivPhotoPreview.setImageBitmap(selectedImageBitmap);
                        ivPhotoPreview.setVisibility(View.VISIBLE);

                        photoFileName = "photo_" + System.currentTimeMillis() + ".jpg";
                        tvPhotoStatus.setText("Photo captured: " + photoFileName);
                        tvPhotoStatus.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
                    }
                });

        // Map Launcher
        mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("location_address");
                        if (address != null) {
                            etReportLocation.setText(address);
                            Toast.makeText(this, "Location selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Set click listeners
     */
    private void setClickListeners() {
        // Back button
        btnBackSubmitReport.setOnClickListener(v -> finish());

        // Select photo button
        btnSelectPhoto.setOnClickListener(v -> showPhotoSelectionDialog());

        // Submit report button
        btnSubmitReport.setOnClickListener(v -> submitReport());

        // Map button (End Icon)
        tilReportLocation.setEndIconOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapLauncher.launch(intent);
        });
    }

    /**
     * Show dialog to choose between Camera and Gallery
     */
    private void showPhotoSelectionDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Take Photo
                    if (checkCameraPermission()) {
                        openCamera();
                    }
                    break;
                case 1:
                    // Choose from Gallery
                    if (checkStoragePermission()) {
                        openGallery();
                    }
                    break;
                case 2:
                    // Cancel
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    /**
     * Check camera permission
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    /**
     * Check storage permission
     */
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * Open camera
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open gallery
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAutoLocation();
            } else {
                Toast.makeText(this, "Location permission is required for auto-detection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Automatically detect current location using GPS
     */
    private void getAutoLocation() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_CODE);
            return;
        }

        // Show loading indicator
        etReportLocation.setHint("📍 Detecting location...");

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    // Convert coordinates to address using Geocoder
                    try {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), 
                                location.getLongitude(), 
                                1);
                        
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            StringBuilder locationText = new StringBuilder();
                            
                            // Build address string
                            if (address.getLocality() != null) {
                                locationText.append(address.getLocality());
                            }
                            if (address.getSubAdminArea() != null) {
                                if (locationText.length() > 0) locationText.append(", ");
                                locationText.append(address.getSubAdminArea());
                            }
                            if (address.getAdminArea() != null) {
                                if (locationText.length() > 0) locationText.append(", ");
                                locationText.append(address.getAdminArea());
                            }
                            
                            if (locationText.length() > 0) {
                                etReportLocation.setText(locationText.toString());
                                Toast.makeText(this, "📍 Location detected!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Fallback to coordinates
                                etReportLocation.setText(String.format(Locale.US, 
                                    "%.4f, %.4f", 
                                    location.getLatitude(), 
                                    location.getLongitude()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Fallback to coordinates
                        etReportLocation.setText(String.format(Locale.US, 
                            "%.4f, %.4f", 
                            location.getLatitude(), 
                            location.getLongitude()));
                    }
                } else {
                    etReportLocation.setHint("Add location manually");
                }
            })
            .addOnFailureListener(this, e -> {
                etReportLocation.setHint("Add location manually");
            });
    }

    /**
     * Submit the report to backend
     */
    private void submitReport() {
        String title = etReportTitle.getText().toString().trim();
        String description = etReportDescription.getText().toString().trim();
        String location = etReportLocation.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            etReportTitle.setError("Title is required");
            etReportTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etReportDescription.setError("Description is required");
            etReportDescription.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etReportLocation.setError("Location is required");
            etReportLocation.requestFocus();
            return;
        }

        // Create request (photoUrl can be added later when photo upload is implemented)
        CreateReportRequest request = new CreateReportRequest(
                title + ": " + description,  // Combine title and description
                selectedImageBitmap != null ? photoFileName : null,  // Placeholder for photo URL
                location
        );

        // Call backend API
        reportApi.createReport(request).enqueue(new Callback<PollutionReportResponse>() {
            @Override
            public void onResponse(@NonNull Call<PollutionReportResponse> call,
                                 @NonNull Response<PollutionReportResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SubmitReportActivity.this,
                            "Failed to submit report: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(SubmitReportActivity.this,
                        "✅ Report submitted successfully!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<PollutionReportResponse> call,
                                @NonNull Throwable t) {
                Toast.makeText(SubmitReportActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Submit the report (deprecated - saves locally)
     */
    private void submitReportLocal() {
        String title = etReportTitle.getText().toString().trim();
        String description = etReportDescription.getText().toString().trim();
        String location = etReportLocation.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            etReportTitle.setError("Title is required");
            etReportTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etReportDescription.setError("Description is required");
            etReportDescription.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etReportLocation.setError("Location is required");
            etReportLocation.requestFocus();
            return;
        }

        try {
            // Get existing reports
            String reportsJson = sharedPreferences.getString("reports", "[]");
            JSONArray reportsArray = new JSONArray(reportsJson);

            // Create new report
            JSONObject newReport = new JSONObject();
            newReport.put("title", title);
            newReport.put("description", description);
            newReport.put("location", location);
            newReport.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.US).format(new Date()));
            newReport.put("status", "Pending");

            if (selectedImageBitmap != null) {
                newReport.put("hasPhoto", true);
                newReport.put("photoFileName", photoFileName);
                // In a real app, you would save the image to internal storage here
            } else {
                newReport.put("hasPhoto", false);
            }

            // Add to array
            reportsArray.put(newReport);

            // Save back to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("reports", reportsArray.toString());
            editor.apply();

            Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show();
        }
    }
}
