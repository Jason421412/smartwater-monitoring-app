package com.smartwater.monitoring.network;

import java.util.Map;

import com.smartwater.monitoring.network.dto.LoginReq;
import com.smartwater.monitoring.network.dto.LoginResp;
import com.smartwater.monitoring.network.dto.RegisterRequest;
import com.smartwater.monitoring.network.dto.ResendVerificationRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("api/users/register")
    Call<Map<String, Object>> register(@Body RegisterRequest body);

    @POST("api/users/login")
    Call<LoginResp> login(@Body LoginReq body);

    @POST("api/users/resend-verification")
    Call<Map<String, Object>> resendVerification(@Body ResendVerificationRequest body);

    @GET("api/users/verify-email")
    Call<Map<String, Object>> verifyEmail(@Query("token") String token);
}
