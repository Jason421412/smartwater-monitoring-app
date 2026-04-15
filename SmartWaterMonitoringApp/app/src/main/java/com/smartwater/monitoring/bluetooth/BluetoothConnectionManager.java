package com.smartwater.monitoring.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * BluetoothConnectionManager handles real Bluetooth RFCOMM/SPP connections
 * to external sensor devices like Arduino, ESP32, HC-05, etc.
 */
public class BluetoothConnectionManager {

    private static final String TAG = "BluetoothConnection";
    
    // Standard Serial Port Profile (SPP) UUID for Bluetooth serial communication
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice currentDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    
    private ConnectedThread connectedThread;
    private ConnectThread connectThread;
    
    private ConnectionCallback callback;
    private Handler mainHandler;
    
    private volatile boolean isConnected = false;
    
    /**
     * Callback interface for Bluetooth connection events
     */
    public interface ConnectionCallback {
        void onConnecting(String deviceName);
        void onConnected(String deviceName, String macAddress);
        void onConnectionFailed(String error);
        void onDisconnected(String reason);
        void onDataReceived(String data);
        void onSensorDataParsed(double ph, double temperature, Double turbidity);
    }
    
    public BluetoothConnectionManager(ConnectionCallback callback) {
        this.callback = callback;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Connect to a Bluetooth device
     * @param device The BluetoothDevice to connect to
     */
    public void connect(BluetoothDevice device) {
        if (device == null) {
            notifyConnectionFailed("Device is null");
            return;
        }
        
        // Cancel any existing connection
        disconnect();
        
        currentDevice = device;
        notifyConnecting(getDeviceName(device));
        
        // Start connection in background thread
        connectThread = new ConnectThread(device);
        connectThread.start();
    }
    
    /**
     * Disconnect from current device
     */
    public void disconnect() {
        isConnected = false;
        
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        
        closeSocket();
    }
    
    /**
     * Send data to connected device
     */
    public void sendData(String data) {
        if (connectedThread != null && isConnected) {
            connectedThread.write(data.getBytes());
        }
    }
    
    /**
     * Check if currently connected
     */
    public boolean isConnected() {
        return isConnected && bluetoothSocket != null && bluetoothSocket.isConnected();
    }
    
    /**
     * Get current device name
     */
    public String getCurrentDeviceName() {
        return currentDevice != null ? getDeviceName(currentDevice) : null;
    }
    
    /**
     * Get current device MAC address
     */
    public String getCurrentMacAddress() {
        try {
            return currentDevice != null ? currentDevice.getAddress() : null;
        } catch (SecurityException e) {
            return null;
        }
    }
    
    private String getDeviceName(BluetoothDevice device) {
        try {
            String name = device.getName();
            return name != null ? name : device.getAddress();
        } catch (SecurityException e) {
            return "Unknown Device";
        }
    }
    
    private void closeSocket() {
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket: " + e.getMessage());
        }
    }
    
    // ==================== Notification Methods ====================
    
    private void notifyConnecting(String deviceName) {
        mainHandler.post(() -> {
            if (callback != null) callback.onConnecting(deviceName);
        });
    }
    
    private void notifyConnected(String deviceName, String macAddress) {
        mainHandler.post(() -> {
            if (callback != null) callback.onConnected(deviceName, macAddress);
        });
    }
    
    private void notifyConnectionFailed(String error) {
        mainHandler.post(() -> {
            if (callback != null) callback.onConnectionFailed(error);
        });
    }
    
    private void notifyDisconnected(String reason) {
        mainHandler.post(() -> {
            if (callback != null) callback.onDisconnected(reason);
        });
    }
    
    private void notifyDataReceived(String data) {
        mainHandler.post(() -> {
            if (callback != null) callback.onDataReceived(data);
        });
    }
    
    private void notifySensorData(double ph, double temperature, Double turbidity) {
        mainHandler.post(() -> {
            if (callback != null) callback.onSensorDataParsed(ph, temperature, turbidity);
        });
    }
    
    // ==================== Connection Thread ====================
    
    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;
        
        ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            
            try {
                // Create RFCOMM socket using SPP UUID
                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "Socket creation failed: " + e.getMessage());
            }
            
            socket = tmp;
        }
        
        @Override
        public void run() {
            // Cancel discovery to speed up connection
            try {
                bluetoothAdapter.cancelDiscovery();
            } catch (SecurityException e) {
                Log.w(TAG, "Could not cancel discovery: " + e.getMessage());
            }
            
            if (socket == null) {
                notifyConnectionFailed("Failed to create socket");
                return;
            }
            
            try {
                // Blocking call - connect to device
                socket.connect();
                
                // Connection successful
                bluetoothSocket = socket;
                isConnected = true;
                
                // Get streams
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                
                String deviceName = getDeviceName(device);
                String macAddress;
                try {
                    macAddress = device.getAddress();
                } catch (SecurityException e) {
                    macAddress = "Unknown";
                }
                
                notifyConnected(deviceName, macAddress);
                
                // Start listening thread
                connectedThread = new ConnectedThread();
                connectedThread.start();
                
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                closeSocket();
                
                // Try fallback connection method for older devices
                try {
                    Log.d(TAG, "Trying fallback connection method...");
                    socket = (BluetoothSocket) device.getClass()
                            .getMethod("createRfcommSocket", int.class)
                            .invoke(device, 1);
                    
                    if (socket != null) {
                        socket.connect();
                        bluetoothSocket = socket;
                        isConnected = true;
                        
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        
                        String deviceName = getDeviceName(device);
                        String macAddress;
                        try {
                            macAddress = device.getAddress();
                        } catch (SecurityException ex) {
                            macAddress = "Unknown";
                        }
                        
                        notifyConnected(deviceName, macAddress);
                        
                        connectedThread = new ConnectedThread();
                        connectedThread.start();
                        return;
                    }
                } catch (Exception fallbackError) {
                    Log.e(TAG, "Fallback connection also failed: " + fallbackError.getMessage());
                }
                
                notifyConnectionFailed("Could not connect: " + e.getMessage());
            }
        }
        
        void cancel() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close socket: " + e.getMessage());
            }
        }
    }
    
    // ==================== Connected Thread (Data Receiving) ====================
    
    private class ConnectedThread extends Thread {
        private volatile boolean running = true;
        private StringBuilder dataBuffer = new StringBuilder();
        
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            
            while (running && isConnected) {
                try {
                    if (inputStream != null && inputStream.available() > 0) {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            String received = new String(buffer, 0, bytes);
                            dataBuffer.append(received);
                            
                            // Process complete lines
                            processBuffer();
                        }
                    } else {
                        // Small delay to prevent busy waiting
                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    if (running) {
                        Log.e(TAG, "Connection lost: " + e.getMessage());
                        isConnected = false;
                        notifyDisconnected("Connection lost");
                    }
                    break;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
        /**
         * Process buffered data - looks for complete lines ending with \n or \r\n
         */
        private void processBuffer() {
            String data = dataBuffer.toString();
            int newlineIndex;
            
            while ((newlineIndex = data.indexOf('\n')) != -1) {
                String line = data.substring(0, newlineIndex).trim();
                data = data.substring(newlineIndex + 1);
                
                if (!line.isEmpty()) {
                    notifyDataReceived(line);
                    parseSensorData(line);
                }
            }
            
            dataBuffer = new StringBuilder(data);
        }
        
        /**
         * Parse sensor data from received line
         * Supports multiple formats:
         * - "pH:7.2,temp:25.5"
         * - "pH:7.2,temp:25.5,turb:100"
         * - "7.2,25.5" (pH, temp)
         * - JSON: {"ph":7.2,"temp":25.5}
         */
        private void parseSensorData(String line) {
            try {
                double ph = -1;
                double temp = -1;
                Double turbidity = null;
                
                // Format 1: key:value pairs (e.g., "pH:7.2,temp:25.5,turb:100")
                if (line.contains(":")) {
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        String[] keyValue = part.trim().split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().toLowerCase();
                            String value = keyValue[1].trim();
                            
                            if (key.equals("ph")) {
                                ph = Double.parseDouble(value);
                            } else if (key.contains("temp") || key.equals("t")) {
                                temp = Double.parseDouble(value);
                            } else if (key.contains("turb") || key.equals("ntu")) {
                                turbidity = Double.parseDouble(value);
                            }
                        }
                    }
                }
                // Format 2: comma-separated values (e.g., "7.2,25.5" or "7.2,25.5,100")
                else if (line.contains(",")) {
                    String[] values = line.split(",");
                    if (values.length >= 2) {
                        ph = Double.parseDouble(values[0].trim());
                        temp = Double.parseDouble(values[1].trim());
                        if (values.length >= 3) {
                            turbidity = Double.parseDouble(values[2].trim());
                        }
                    }
                }
                // Format 3: Simple JSON (e.g., {"ph":7.2,"temp":25.5})
                else if (line.startsWith("{") && line.endsWith("}")) {
                    // Basic JSON parsing without library
                    if (line.contains("\"ph\"")) {
                        ph = extractJsonDouble(line, "ph");
                    }
                    if (line.contains("\"temp\"")) {
                        temp = extractJsonDouble(line, "temp");
                    }
                    if (line.contains("\"turb\"")) {
                        turbidity = extractJsonDouble(line, "turb");
                    }
                }
                
                // Validate and notify
                if (ph >= 0 && ph <= 14 && temp >= -40 && temp <= 100) {
                    notifySensorData(ph, temp, turbidity);
                }
                
            } catch (NumberFormatException e) {
                Log.w(TAG, "Could not parse sensor data: " + line);
            }
        }
        
        private double extractJsonDouble(String json, String key) {
            try {
                int keyIndex = json.indexOf("\"" + key + "\"");
                if (keyIndex != -1) {
                    int colonIndex = json.indexOf(":", keyIndex);
                    if (colonIndex != -1) {
                        int start = colonIndex + 1;
                        int end = start;
                        while (end < json.length() && 
                               (Character.isDigit(json.charAt(end)) || 
                                json.charAt(end) == '.' || 
                                json.charAt(end) == '-')) {
                            end++;
                        }
                        return Double.parseDouble(json.substring(start, end).trim());
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "JSON parse error for key " + key);
            }
            return -1;
        }
        
        void write(byte[] bytes) {
            try {
                if (outputStream != null) {
                    outputStream.write(bytes);
                    outputStream.flush();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing data: " + e.getMessage());
            }
        }
        
        void cancel() {
            running = false;
        }
    }
}
