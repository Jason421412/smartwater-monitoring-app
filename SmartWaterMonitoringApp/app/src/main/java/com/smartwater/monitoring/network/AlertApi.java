package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.AlertResponse;
import com.smartwater.monitoring.network.dto.WaterReadingRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Retrofit API interface for Alert endpoints
 */
public interface AlertApi {

    /**
     * Evaluate water reading and check for alerts
     * POST /api/alerts/evaluate
     */
    @POST("alerts/evaluate")
    Call<AlertResponse> evaluate(@Body WaterReadingRequest request);

    /**
     * Get alert history for current user
     * GET /api/alerts/me
     */
    @GET("alerts/me")
    Call<List<AlertResponse>> getMyAlerts();

    /**
     * Get all alerts (admin/public)
     * GET /api/alerts
     */
    @GET("alerts")
    Call<List<AlertResponse>> getAllAlerts();
}
