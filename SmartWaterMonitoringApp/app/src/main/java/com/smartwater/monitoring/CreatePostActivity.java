package com.smartwater.monitoring;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.CommunityApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.CommunityPostRequest;
import com.smartwater.monitoring.network.dto.CommunityPostResponse;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CreatePostActivity - Twitter-style post creation
 * Allows users to create community posts with photos and sensor data
 */
public class CreatePostActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBackCreatePost, btnAddPhoto, btnAddSensorData, btnAddHashtag, btnOpenMap;
    private ImageButton btnRemovePhoto, btnRemoveSensor;
    private android.widget.Button btnCreatePost;
    private EditText etPostContent, etPostLocation;
    private TextView tvUserName, tvSensorDataStatus, tvCharCount;
    private ImageView ivUserAvatar, ivPhotoPreview;
    private CardView cvPhotoPreview;
    private LinearLayout llSensorData;

    // Data
    private SharedPreferences sharedPreferences;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private boolean hasSensorData = false;
    private Double phValue = null;
    private Double tempValue = null;

    // Backend API
    private CommunityApi communityApi;
    private TokenStore tokenStore;

    // Constants
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private static final int LOCATION_PERMISSION_CODE = 103;
    private static final int MAX_CHAR_COUNT = 280;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> mapLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_create_post);

        // Initialize
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        tokenStore = new TokenStore(this);
        communityApi = ApiClient.createCommunity(this, () -> tokenStore.getToken());

        initializeViews();
        initializeActivityResultLaunchers();
        setClickListeners();
        setupCharacterCount();
        loadUserData();
        
        // Initialize location client and get auto location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getAutoLocation();
    }

    private void initializeViews() {
        btnBackCreatePost = findViewById(R.id.btnBackCreatePost);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnAddSensorData = findViewById(R.id.btnAddSensorData);
        btnAddHashtag = findViewById(R.id.btnAddHashtag);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        btnRemoveSensor = findViewById(R.id.btnRemoveSensor);
        
        etPostContent = findViewById(R.id.etPostContent);
        etPostLocation = findViewById(R.id.etPostLocation);
        
        tvUserName = findViewById(R.id.tvUserName);
        tvSensorDataStatus = findViewById(R.id.tvSensorDataStatus);
        tvCharCount = findViewById(R.id.tvCharCount);
        
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
        cvPhotoPreview = findViewById(R.id.cvPhotoPreview);
        llSensorData = findViewById(R.id.llSensorData);
    }

    private void loadUserData() {
        String username = sharedPreferences.getString("userName", "User");
        tvUserName.setText(username);
        
        // Set avatar color based on username
        int[] colors = {0xFF1DA1F2, 0xFF17BF63, 0xFFF45D22, 0xFF794BC4};
        int colorIndex = Math.abs(username.hashCode()) % colors.length;
        ivUserAvatar.setColorFilter(colors[colorIndex]);
    }

    private void setupCharacterCount() {
        etPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remaining = MAX_CHAR_COUNT - s.length();
                tvCharCount.setText(String.valueOf(remaining));
                
                if (remaining < 0) {
                    tvCharCount.setTextColor(0xFFE0245E); // Red
                } else if (remaining < 20) {
                    tvCharCount.setTextColor(0xFFFFAD1F); // Yellow
                } else {
                    tvCharCount.setTextColor(0xFF657786); // Gray
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initializeActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), selectedImageUri);
                            showPhotoPreview();
                            Toast.makeText(this, "📷 Photo added!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        selectedImageBitmap = (Bitmap) extras.get("data");
                        showPhotoPreview();
                        Toast.makeText(this, "📷 Photo captured!", Toast.LENGTH_SHORT).show();
                    }
                });

        mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("location_address");
                        if (address != null) {
                            etPostLocation.setText(address);
                            Toast.makeText(this, "Location selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showPhotoPreview() {
        ivPhotoPreview.setImageBitmap(selectedImageBitmap);
        cvPhotoPreview.setVisibility(View.VISIBLE);
    }

    private void removePhoto() {
        selectedImageBitmap = null;
        selectedImageUri = null;
        cvPhotoPreview.setVisibility(View.GONE);
    }

    private void setClickListeners() {
        btnBackCreatePost.setOnClickListener(v -> finish());

        btnAddPhoto.setOnClickListener(v -> showPhotoSelectionDialog());
        
        btnRemovePhoto.setOnClickListener(v -> removePhoto());

        // Open Map button
        btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapLauncher.launch(intent);
        });

        btnAddSensorData.setOnClickListener(v -> addSensorData());
        
        btnRemoveSensor.setOnClickListener(v -> removeSensorData());

        btnAddHashtag.setOnClickListener(v -> {
            String current = etPostContent.getText().toString();
            etPostContent.setText(current + " #WaterQuality ");
            etPostContent.setSelection(etPostContent.getText().length());
        });

        btnCreatePost.setOnClickListener(v -> createPost());
    }

    private void addSensorData() {
        String ph = sharedPreferences.getString("lastPhValue", "7.0");
        String temp = sharedPreferences.getString("lastTemperature", "25.0");
        
        try {
            phValue = Double.parseDouble(ph);
            tempValue = Double.parseDouble(temp);
        } catch (NumberFormatException e) {
            phValue = 7.0;
            tempValue = 25.0;
        }

        hasSensorData = true;
        tvSensorDataStatus.setText("pH: " + ph + " | Temp: " + temp + "°C");
        llSensorData.setVisibility(View.VISIBLE);
        
        Toast.makeText(this, "📊 Sensor data added!", Toast.LENGTH_SHORT).show();
    }

    private void removeSensorData() {
        hasSensorData = false;
        phValue = null;
        tempValue = null;
        llSensorData.setVisibility(View.GONE);
    }

    private void showPhotoSelectionDialog() {
        String[] options = {"📷 Take Photo", "🖼️ Choose from Gallery", "Cancel"};

        new AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        if (checkCameraPermission()) openCamera();
                        break;
                    case 1:
                        if (checkStoragePermission()) openGallery();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                }
            })
            .show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return false;
        }
        return true;
    }

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

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private void createPost() {
        String content = etPostContent.getText().toString().trim();
        String location = etPostLocation.getText().toString().trim();

        // Validate
        if (content.isEmpty()) {
            Toast.makeText(this, "Post content is required", Toast.LENGTH_SHORT).show();
            etPostContent.requestFocus();
            return;
        }

        if (content.length() > MAX_CHAR_COUNT) {
            Toast.makeText(this, "Post exceeds 280 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.isEmpty()) {
            location = "Unknown Location";
        }

        // Create request
        CommunityPostRequest request = new CommunityPostRequest(content, location);
        
        if (hasSensorData) {
            request.setPh(phValue);
            request.setTemperature(tempValue);
        }
        
        request.setType("INFO");

        // Show loading
        btnCreatePost.setEnabled(false);
        btnCreatePost.setText("Posting...");

        // Send to backend
        communityApi.createPost(request).enqueue(new Callback<CommunityPostResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommunityPostResponse> call,
                                   @NonNull Response<CommunityPostResponse> response) {
                btnCreatePost.setEnabled(true);
                btnCreatePost.setText("Post");

                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, 
                        "✅ Post shared with community!", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreatePostActivity.this, 
                        "Failed to create post: " + response.code(), 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommunityPostResponse> call, @NonNull Throwable t) {
                btnCreatePost.setEnabled(true);
                btnCreatePost.setText("Post");
                
                Toast.makeText(CreatePostActivity.this, 
                    "Network error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
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
        etPostLocation.setHint("📍 Detecting location...");

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
                                etPostLocation.setText(locationText.toString());
                                Toast.makeText(this, "📍 Location detected!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Fallback to coordinates
                                etPostLocation.setText(String.format(Locale.US, 
                                    "%.4f, %.4f", 
                                    location.getLatitude(), 
                                    location.getLongitude()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Fallback to coordinates
                        etPostLocation.setText(String.format(Locale.US, 
                            "%.4f, %.4f", 
                            location.getLatitude(), 
                            location.getLongitude()));
                    }
                } else {
                    etPostLocation.setHint("Add location...");
                }
            })
            .addOnFailureListener(this, e -> {
                etPostLocation.setHint("Add location...");
            });
    }
}
