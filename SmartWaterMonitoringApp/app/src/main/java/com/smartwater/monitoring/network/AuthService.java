package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.RegisterRequest;
import com.smartwater.monitoring.network.dto.ResendVerificationRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("api/users/register")
    Call<Map<String, Object>> register(@Body RegisterRequest body);

    @POST("api/users/resend-verification")
    Call<Map<String, Object>> resendVerification(@Body ResendVerificationRequest body);
}
