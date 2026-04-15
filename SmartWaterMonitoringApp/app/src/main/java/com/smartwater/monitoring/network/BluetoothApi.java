package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.BluetoothConnectionStatusRequest;
import com.smartwater.monitoring.network.dto.BluetoothDeviceResponse;
import com.smartwater.monitoring.network.dto.BluetoothPairRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Retrofit API interface for Bluetooth endpoints
 */
public interface BluetoothApi {

    /**
     * Pair a new Bluetooth device
     * POST /api/bluetooth/pair
     */
    @POST("api/bluetooth/pair")
    Call<BluetoothDeviceResponse> pairDevice(@Body BluetoothPairRequest request);

    /**
     * Update Bluetooth connection status
     * POST /api/bluetooth/status
     */
    @POST("api/bluetooth/status")
    Call<BluetoothDeviceResponse> updateStatus(@Body BluetoothConnectionStatusRequest request);

    /**
     * Get all paired devices for current user
     * GET /api/bluetooth/me/devices
     */
    @GET("api/bluetooth/me/devices")
    Call<List<BluetoothDeviceResponse>> getMyDevices();
}
