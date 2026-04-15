package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.SensorDataResponse;
import com.smartwater.monitoring.network.dto.WaterIngestRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WaterApi {

    // ✅ Module 2：Android -> Spring -> (forward to FastAPI)
    @POST("api/water/ingest")
    Call<Map<String, Object>> ingest(@Body WaterIngestRequest req);

    // ✅ Dashboard - Latest reading for specific device (Forwarded to FastAPI via Spring)
    @GET("api/sensor/device/{deviceId}/latest")
    Call<Map<String, Object>> getDeviceLatest(@Path("deviceId") String deviceId);

    // ✅ Dashboard - Latest reading (User bound)
    @GET("api/sensor/me/latest")
    Call<Map<String, Object>> getMyLatest();

    // ✅ Dashboard - Summary (24h)
    @GET("api/sensor/me/summary")
    Call<Map<String, Object>> getMySummary(
            @Query("from") String from,
            @Query("to") String to
    );

    // ✅ Dashboard - History for charts
    @GET("api/sensor/me/range")
    Call<List<SensorDataResponse>> getMyHistory(
            @Query("from") String from,
            @Query("to") String to
    );

    // ✅ Dashboard - History for charts (Device specific)
    @GET("api/me/range")
    Call<List<SensorDataResponse>> getDeviceHistory(
            @Query("email") String deviceId,
            @Query("from") String from,
            @Query("to") String to
    );
}
