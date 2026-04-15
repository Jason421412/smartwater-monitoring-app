package com.smartwater.monitoring;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartwater.monitoring.bluetooth.BluetoothConnectionManager;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.BluetoothApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.BluetoothConnectionStatusRequest;
import com.smartwater.monitoring.network.dto.BluetoothDeviceResponse;
import com.smartwater.monitoring.network.dto.BluetoothPairRequest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * BluetoothActivity handles real Bluetooth device scanning and connection
 * for water quality monitoring sensors
 */
public class BluetoothActivity extends AppCompatActivity implements BluetoothConnectionManager.ConnectionCallback {

    // UI Components
    private ImageButton btnProfileIcon, btnLogoutIcon;
    private Button btnScanDevices, btnDisconnect;
    private ListView lvBluetoothDevices;
    private TextView tvConnectionStatus, tvPhValue, tvTemperatureValue, tvTurbidityValue;
    private View statusIndicator;
    private BottomNavigationView bottomNavigation;

    // Navigation Helper
    private NavigationHelper navigationHelper;

    // Bluetooth components
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnectionManager connectionManager;
    private ArrayList<BluetoothDevice> discoveredDevices;
    private ArrayList<String> deviceDisplayList;
    private ArrayAdapter<String> deviceAdapter;

    // Discovery
    private boolean isDiscovering = false;

    // SharedPreferences
    private SharedPreferences sharedPreferences;

    // Backend API
    private BluetoothApi bluetoothApi;
    private TokenStore tokenStore;

    // Notification
    private static final String CHANNEL_ID = "bluetooth_notifications";
    private static final int NOTIFICATION_ID = 1001;

    // Permission request codes
    private static final int BLUETOOTH_PERMISSION_CODE = 100;
    private static final int ENABLE_BLUETOOTH_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Initialize navigation helper
        navigationHelper = new NavigationHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // Initialize Backend API
        tokenStore = new TokenStore(this);
        bluetoothApi = ApiClient.createBluetooth(this, () -> tokenStore.getToken());

        // Initialize notification channel
        createNotificationChannel();

        // Initialize components
        initializeViews();
        initializeBluetooth();
        setClickListeners();
        setupNavigation();

        // Register discovery receiver
        registerDiscoveryReceiver();
    }

    private void initializeViews() {
        btnProfileIcon = findViewById(R.id.btnProfileIcon);
        btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        btnScanDevices = findViewById(R.id.btnScanDevices);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        lvBluetoothDevices = findViewById(R.id.lvBluetoothDevices);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvPhValue = findViewById(R.id.tvPhValue);
        tvTemperatureValue = findViewById(R.id.tvTemperatureValue);
        statusIndicator = findViewById(R.id.statusIndicator);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Note: tvTurbidityValue is optional - may not exist in layout
        // tvTurbidityValue = findViewById(R.id.tvTurbidityValue);

        // Initialize device lists
        discoveredDevices = new ArrayList<>();
        deviceDisplayList = new ArrayList<>();
        
        deviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, deviceDisplayList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                
                BluetoothDevice device = discoveredDevices.get(position);
                try {
                    String name = device.getName();
                    text1.setText(name != null ? name : "Unknown Device");
                    text2.setText(device.getAddress());
                } catch (SecurityException e) {
                    text1.setText("Unknown Device");
                    text2.setText("No permission");
                }
                
                return view;
            }
        };
        lvBluetoothDevices.setAdapter(deviceAdapter);

        // Initial state
        btnDisconnect.setEnabled(false);
    }

    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectionManager = new BluetoothConnectionManager(this);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            btnScanDevices.setEnabled(false);
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);
            } catch (SecurityException e) {
                Toast.makeText(this, "Please enable Bluetooth manually", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setClickListeners() {
        btnScanDevices.setOnClickListener(v -> scanForDevices());

        lvBluetoothDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (position < discoveredDevices.size()) {
                connectToDevice(discoveredDevices.get(position));
            }
        });

        btnDisconnect.setOnClickListener(v -> disconnectDevice());
    }

    private void setupNavigation() {
        navigationHelper.setupTopBar(btnProfileIcon, btnLogoutIcon);
        navigationHelper.setupBottomNavigation(bottomNavigation, R.id.navigation_bluetooth);
    }

    // ==================== Scanning ====================

    private void scanForDevices() {
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        // Check if Location is enabled (required for Bluetooth scanning on Android)
        if (!isLocationEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("Android requires Location to be enabled for Bluetooth scanning. Please enable Location services.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth Disabled")
                    .setMessage("Please enable Bluetooth to scan for devices.")
                    .setPositiveButton("Enable", (dialog, which) -> {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        try {
                            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);
                        } catch (SecurityException e) {
                            Toast.makeText(this, "Please enable Bluetooth manually", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        discoveredDevices.clear();
        deviceDisplayList.clear();

        // Add paired devices first
        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                discoveredDevices.add(device);
                String name = device.getName();
                deviceDisplayList.add((name != null ? name : "Unknown") + " [Paired]");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission required to access paired devices", Toast.LENGTH_SHORT).show();
        }

        deviceAdapter.notifyDataSetChanged();

        // Start discovery for new devices
        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            
            isDiscovering = bluetoothAdapter.startDiscovery();
            if (isDiscovering) {
                btnScanDevices.setText("Scanning...");
                btnScanDevices.setEnabled(false);
                
                // Stop after 12 seconds
                new Handler().postDelayed(() -> {
                    stopDiscovery();
                }, 12000);
                
                Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Could not start scan. Make sure Bluetooth and Location are ON.", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied for Bluetooth scan", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopDiscovery() {
        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        } catch (SecurityException e) {
            // Ignore
        }
        isDiscovering = false;
        btnScanDevices.setText("Scan for Devices");
        btnScanDevices.setEnabled(true);
        
        Toast.makeText(this, "Found " + discoveredDevices.size() + " devices", Toast.LENGTH_SHORT).show();
    }

    // ==================== Connection ====================

    private void connectToDevice(BluetoothDevice device) {
        // Stop discovery first
        stopDiscovery();
        
        try {
            String deviceName = device.getName();
            new AlertDialog.Builder(this)
                    .setTitle("Connect to Device")
                    .setMessage("Connect to " + (deviceName != null ? deviceName : device.getAddress()) + "?")
                    .setPositiveButton("Connect", (dialog, which) -> {
                        connectionManager.connect(device);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnectDevice() {
        connectionManager.disconnect();
        
        // Update backend
        String macAddress = connectionManager.getCurrentMacAddress();
        if (macAddress != null) {
            updateConnectionStatusBackend(macAddress, "DISCONNECTED", "User disconnected");
        }
        
        showDisconnectNotification();
    }

    // ==================== BluetoothConnectionManager Callbacks ====================

    @Override
    public void onConnecting(String deviceName) {
        tvConnectionStatus.setText("Connecting to " + deviceName + "...");
        statusIndicator.setBackgroundResource(android.R.drawable.presence_away);
        btnScanDevices.setEnabled(false);
        btnDisconnect.setEnabled(false);
    }

    @Override
    public void onConnected(String deviceName, String macAddress) {
        tvConnectionStatus.setText("Connected to " + deviceName);
        statusIndicator.setBackgroundResource(android.R.drawable.presence_online);
        btnScanDevices.setEnabled(false);
        btnDisconnect.setEnabled(true);

        Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show();

        // Register with backend
        pairDeviceWithBackend(deviceName, macAddress);
        updateConnectionStatusBackend(macAddress, "CONNECTED", "Device connected");
    }

    @Override
    public void onConnectionFailed(String error) {
        tvConnectionStatus.setText("Connection failed");
        statusIndicator.setBackgroundResource(android.R.drawable.presence_offline);
        btnScanDevices.setEnabled(true);
        btnDisconnect.setEnabled(false);

        Toast.makeText(this, "Connection failed: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected(String reason) {
        tvConnectionStatus.setText("Disconnected: " + reason);
        statusIndicator.setBackgroundResource(android.R.drawable.presence_offline);
        btnScanDevices.setEnabled(true);
        btnDisconnect.setEnabled(false);

        tvPhValue.setText("--");
        tvTemperatureValue.setText("-- °C");
        if (tvTurbidityValue != null) {
            tvTurbidityValue.setText("-- NTU");
        }

        Toast.makeText(this, "Disconnected: " + reason, Toast.LENGTH_SHORT).show();
        showDisconnectNotification();
    }

    @Override
    public void onDataReceived(String data) {
        // Raw data logging (optional)
        android.util.Log.d("BluetoothData", "Received: " + data);
    }

    @Override
    public void onSensorDataParsed(double ph, double temperature, Double turbidity) {
        // Update UI
        tvPhValue.setText(String.format(Locale.US, "%.2f", ph));
        tvTemperatureValue.setText(String.format(Locale.US, "%.1f °C", temperature));
        
        if (tvTurbidityValue != null && turbidity != null) {
            tvTurbidityValue.setText(String.format(Locale.US, "%.1f NTU", turbidity));
        }

        // Save to SharedPreferences
        saveSensorData(ph, temperature, turbidity);
    }

    // ==================== BroadcastReceiver for Discovery ====================

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    try {
                        String name = device.getName();
                        deviceDisplayList.add(name != null ? name : device.getAddress());
                    } catch (SecurityException e) {
                        deviceDisplayList.add(device.getAddress());
                    }
                    deviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                stopDiscovery();
            }
        }
    };

    private void registerDiscoveryReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, filter);
    }

    // ==================== Helper Methods ====================

    private void saveSensorData(double ph, double temperature, Double turbidity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastPhValue", String.format(Locale.US, "%.2f", ph));
        editor.putString("lastTemperature", String.format(Locale.US, "%.1f", temperature));
        if (turbidity != null) {
            editor.putString("lastTurbidity", String.format(Locale.US, "%.1f", turbidity));
        }
        editor.putLong("lastUpdateTime", System.currentTimeMillis());

        // Save history
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        String historyEntry = timestamp + "," + ph + "," + temperature;
        if (turbidity != null) {
            historyEntry += "," + turbidity;
        }

        String existingHistory = sharedPreferences.getString("sensorHistory", "");
        if (!existingHistory.isEmpty()) {
            existingHistory += ";";
        }
        existingHistory += historyEntry;

        // Keep only last 50 entries
        String[] entries = existingHistory.split(";");
        if (entries.length > 50) {
            StringBuilder newHistory = new StringBuilder();
            for (int i = entries.length - 50; i < entries.length; i++) {
                if (newHistory.length() > 0) newHistory.append(";");
                newHistory.append(entries[i]);
            }
            existingHistory = newHistory.toString();
        }

        editor.putString("sensorHistory", existingHistory);
        editor.apply();
    }

    private boolean isLocationEnabled() {
        android.location.LocationManager locationManager = 
            (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && 
               (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER));
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, BLUETOOTH_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, BLUETOOTH_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                scanForDevices();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ==================== Backend Integration ====================

    private void pairDeviceWithBackend(String deviceName, String macAddress) {
        BluetoothPairRequest request = new BluetoothPairRequest(deviceName, macAddress);
        bluetoothApi.pairDevice(request).enqueue(new retrofit2.Callback<BluetoothDeviceResponse>() {
            @Override
            public void onResponse(retrofit2.Call<BluetoothDeviceResponse> call, retrofit2.Response<BluetoothDeviceResponse> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BluetoothActivity", "✅ Device paired with backend");
                } else {
                    android.util.Log.e("BluetoothActivity", "Failed to pair: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<BluetoothDeviceResponse> call, Throwable t) {
                android.util.Log.e("BluetoothActivity", "Pair error: " + t.getMessage());
            }
        });
    }

    private void updateConnectionStatusBackend(String macAddress, String status, String message) {
        BluetoothConnectionStatusRequest request = new BluetoothConnectionStatusRequest(macAddress, status, message);
        bluetoothApi.updateStatus(request).enqueue(new retrofit2.Callback<BluetoothDeviceResponse>() {
            @Override
            public void onResponse(retrofit2.Call<BluetoothDeviceResponse> call, retrofit2.Response<BluetoothDeviceResponse> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BluetoothActivity", "✅ Status updated: " + status);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<BluetoothDeviceResponse> call, Throwable t) {
                android.util.Log.e("BluetoothActivity", "Status update error: " + t.getMessage());
            }
        });
    }

    // ==================== Notifications ====================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bluetooth Notifications";
            String description = "Alerts for Bluetooth connection status";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showDisconnectNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentTitle("Bluetooth Connection Lost")
                .setContentText("Water monitoring sensor disconnected")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // ==================== Lifecycle ====================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unregister receiver
        try {
            unregisterReceiver(discoveryReceiver);
        } catch (Exception e) {
            // Already unregistered
        }
        
        // Disconnect
        if (connectionManager != null) {
            connectionManager.disconnect();
        }
        
        // Stop discovery
        stopDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH_REQUEST) {
            // Check actual Bluetooth state instead of relying on resultCode
            // (RESULT_OK is unreliable on Android 12+)
            new Handler().postDelayed(() -> {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "Bluetooth enabled ✓", Toast.LENGTH_SHORT).show();
                    // Auto-start scanning after enabling
                    scanForDevices();
                } else {
                    Toast.makeText(this, "Bluetooth is required for this feature", Toast.LENGTH_LONG).show();
                }
            }, 1000); // Wait 1 second for Bluetooth to fully enable
        }
    }
}
