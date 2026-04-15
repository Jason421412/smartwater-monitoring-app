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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

/**
 * UpdateProfileActivity allows users to update their profile information
 * Including name, phone number, and profile picture
 */
public class UpdateProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBackUpdateProfile;
    private ImageView ivProfilePicture;
    private Button btnChangeProfilePicture, btnSaveProfile;
    private TextInputEditText etUpdateName, etUpdatePhone;

    // Data
    private SharedPreferences sharedPreferences;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String profilePictureFileName = "";

    // Permission request codes
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_update_profile);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Initialize activity result launchers
        initializeActivityResultLaunchers();

        // Load current data
        loadCurrentData();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnBackUpdateProfile = findViewById(R.id.btnBackUpdateProfile);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnChangeProfilePicture = findViewById(R.id.btnChangeProfilePicture);
        etUpdateName = findViewById(R.id.etUpdateName);
        etUpdatePhone = findViewById(R.id.etUpdatePhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    /**
     * Initialize Activity Result Launchers
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

                            // Display on ImageView
                            ivProfilePicture.setImageBitmap(selectedImageBitmap);

                            profilePictureFileName = "profile_" + System.currentTimeMillis() + ".jpg";

                            Toast.makeText(this, "Profile picture selected", Toast.LENGTH_SHORT).show();
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

                        // Display on ImageView
                        ivProfilePicture.setImageBitmap(selectedImageBitmap);

                        profilePictureFileName = "profile_" + System.currentTimeMillis() + ".jpg";

                        Toast.makeText(this, "Profile picture captured", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Load current user data
     */
    private void loadCurrentData() {
        String name = sharedPreferences.getString("userName", "");
        String phone = sharedPreferences.getString("userPhone", "");

        etUpdateName.setText(name);
        etUpdatePhone.setText(phone);
    }

    /**
     * Set click listeners
     */
    private void setClickListeners() {
        // Back button
        btnBackUpdateProfile.setOnClickListener(v -> finish());

        // Change profile picture button
        btnChangeProfilePicture.setOnClickListener(v -> showPhotoSelectionDialog());

        // Save profile button
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    /**
     * Show dialog to choose between Camera and Gallery
     */
    private void showPhotoSelectionDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (checkCameraPermission()) {
                        openCamera();
                    }
                    break;
                case 1:
                    if (checkStoragePermission()) {
                        openGallery();
                    }
                    break;
                case 2:
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
        }
    }

    /**
     * Save updated profile
     */
    private void saveProfile() {
        String name = etUpdateName.getText().toString().trim();
        String phone = etUpdatePhone.getText().toString().trim();

        // Validate input
        if (name.isEmpty()) {
            etUpdateName.setError("Name is required");
            etUpdateName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etUpdatePhone.setError("Phone number is required");
            etUpdatePhone.requestFocus();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", name);
        editor.putString("userPhone", phone);

        if (!profilePictureFileName.isEmpty()) {
            editor.putString("profilePicture", profilePictureFileName);
            editor.putBoolean("hasProfilePicture", true);
        }

        editor.apply();

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
