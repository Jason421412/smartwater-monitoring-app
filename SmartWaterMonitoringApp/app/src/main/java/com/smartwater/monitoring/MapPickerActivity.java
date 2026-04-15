package com.smartwater.monitoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvSelectedAddress;
    private Button btnConfirmLocation;
    private String selectedAddress = "";
    private LatLng selectedLatLng;
    private boolean hasMovedToCurrentLocation = false;
    private LocationCallback locationCallback;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Please select a valid location", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("location_address", selectedAddress);
            if (selectedLatLng != null) {
                resultIntent.putExtra("latitude", selectedLatLng.latitude);
                resultIntent.putExtra("longitude", selectedLatLng.longitude);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Check and request permissions
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, setup location on map
                if (mMap != null) {
                    setupLocationOnMap();
                }
            } else {
                Toast.makeText(this, "Location permission is needed to detect your location", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable all gestures and controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Setup location if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupLocationOnMap();
        }

        // Listen for camera idle events (when user stops dragging map)
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            selectedLatLng = center;
            getAddressFromLocation(center.latitude, center.longitude);
        });
    }

    private void setupLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Create location request for real-time updates
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdates(3)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!hasMovedToCurrentLocation && locationResult.getLastLocation() != null) {
                    android.location.Location location = locationResult.getLastLocation();
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                    hasMovedToCurrentLocation = true;
                    // Stop updates after getting location
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        // First try to get last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && !hasMovedToCurrentLocation) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                hasMovedToCurrentLocation = true;
            } else if (!hasMovedToCurrentLocation) {
                // If last location is null, request location updates
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }
            }
        }).addOnFailureListener(e -> {
            // On failure, request location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        });
    }

    private void getAddressFromLocation(double lat, double lng) {
        tvSelectedAddress.setText("Loading address...");

        // Run geocoding on background thread
        executorService.execute(() -> {
            String address = geocodeLocation(lat, lng);
            runOnUiThread(() -> {
                selectedAddress = address;
                tvSelectedAddress.setText(selectedAddress);
            });
        });
    }

    private String geocodeLocation(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                // Build full address
                if (address.getFeatureName() != null && !address.getFeatureName().matches("\\d+")) {
                    sb.append(address.getFeatureName()).append(", ");
                }
                if (address.getThoroughfare() != null) {
                    sb.append(address.getThoroughfare()).append(", ");
                }
                if (address.getSubLocality() != null) {
                    sb.append(address.getSubLocality()).append(", ");
                }
                if (address.getLocality() != null) {
                    sb.append(address.getLocality());
                } else if (address.getSubAdminArea() != null) {
                    sb.append(address.getSubAdminArea());
                }

                String result = sb.toString();
                if (result.endsWith(", ")) {
                    result = result.substring(0, result.length() - 2);
                }

                if (!result.isEmpty()) {
                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return String.format(Locale.US, "%.5f, %.5f", lat, lng);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
