package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.CreateReportRequest;
import com.smartwater.monitoring.network.dto.PollutionReportResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Retrofit API interface for Pollution Report endpoints
 */
public interface ReportApi {

    /**
     * Submit a new pollution report
     * POST /api/reports
     */
    @POST("reports")
    Call<PollutionReportResponse> createReport(@Body CreateReportRequest request);

    /**
     * Get all reports for current user
     * GET /api/reports/me
     */
    @GET("reports/me")
    Call<List<PollutionReportResponse>> getMyReports();

    /**
     * Get all reports (admin/public)
     * GET /api/reports
     */
    @GET("reports")
    Call<List<PollutionReportResponse>> getAllReports();
}
