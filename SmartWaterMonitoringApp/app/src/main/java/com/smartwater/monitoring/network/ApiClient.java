package com.smartwater.monitoring.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {


    // Default backend URL for Android emulator (10.0.2.2 = host machine loopback).
    // For a real device on LAN: change to your server's LAN IP, e.g. "http://192.168.1.x:8080/"
    // For production: replace with your hosted backend URL and enable HTTPS.
    // Can also be changed at runtime via the in-app settings screen (stored in SharedPreferences).
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";
    
    private static final String PREFS_NAME = "SmartWaterPrefs";
    private static final String KEY_BASE_URL = "api_base_url";


    public static String getBaseUrl(Context context) {
        if (context == null) {
            return DEFAULT_BASE_URL;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
    }


    public static void setBaseUrl(Context context, String url) {
        if (context == null || url == null) return;
        

        if (!url.endsWith("/")) {
            url = url + "/";
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BASE_URL, url).apply();
    }

    // ✅ 重置为默认 URL
    public static void resetToDefaultUrl(Context context) {
        setBaseUrl(context, DEFAULT_BASE_URL);
    }

    private static HttpLoggingInterceptor logging() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);
        return log;
    }

    private static OkHttpClient.Builder baseClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging());
    }

    // ✅ 新版：带 baseUrl
    public static AuthApi createAuth(String baseUrl) {
        OkHttpClient client = baseClientBuilder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // must end with /
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(AuthApi.class);
    }

    // ✅ 使用 Context 获取动态 URL
    public static AuthApi createAuth(Context context) {
        return createAuth(getBaseUrl(context));
    }

    // ✅ 兼容旧版：无参（使用默认 URL）
    public static AuthApi createAuth() {
        return createAuth(DEFAULT_BASE_URL);
    }

    // ✅ 新版：带 baseUrl + tokenProvider
    public static WaterApi createWater(String baseUrl, JwtInterceptor.TokenProvider tokenProvider) {
        OkHttpClient client = baseClientBuilder()
                .addInterceptor(new JwtInterceptor(tokenProvider))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // must end with /
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(WaterApi.class);
    }

    // ✅ 使用 Context 获取动态 URL
    public static WaterApi createWater(Context context, JwtInterceptor.TokenProvider tokenProvider) {
        return createWater(getBaseUrl(context), tokenProvider);
    }

    // ✅ 兼容旧版：无参 createWater
    public static WaterApi createWater(JwtInterceptor.TokenProvider tokenProvider) {
        return createWater(DEFAULT_BASE_URL, tokenProvider);
    }

    // ✅ Create BluetoothApi with JWT auth
    public static BluetoothApi createBluetooth(JwtInterceptor.TokenProvider tokenProvider) {
        return createBluetooth(DEFAULT_BASE_URL, tokenProvider);
    }

    public static BluetoothApi createBluetooth(Context context, JwtInterceptor.TokenProvider tokenProvider) {
        return createBluetooth(getBaseUrl(context), tokenProvider);
    }

    public static BluetoothApi createBluetooth(String baseUrl, JwtInterceptor.TokenProvider tokenProvider) {
        OkHttpClient client = baseClientBuilder()
                .addInterceptor(new JwtInterceptor(tokenProvider))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(BluetoothApi.class);
    }

    // ✅ Create CommunityApi with JWT auth
    public static CommunityApi createCommunity(JwtInterceptor.TokenProvider tokenProvider) {
        return createCommunity(DEFAULT_BASE_URL, tokenProvider);
    }

    public static CommunityApi createCommunity(Context context, JwtInterceptor.TokenProvider tokenProvider) {
        return createCommunity(getBaseUrl(context), tokenProvider);
    }

    public static CommunityApi createCommunity(String baseUrl, JwtInterceptor.TokenProvider tokenProvider) {
        OkHttpClient client = baseClientBuilder()
                .addInterceptor(new JwtInterceptor(tokenProvider))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(CommunityApi.class);
    }

    // ✅ Create ReportApi with JWT auth
    public static ReportApi createReport(JwtInterceptor.TokenProvider tokenProvider) {
        return createReport(DEFAULT_BASE_URL, tokenProvider);
    }

    public static ReportApi createReport(Context context, JwtInterceptor.TokenProvider tokenProvider) {
        return createReport(getBaseUrl(context), tokenProvider);
    }

    public static ReportApi createReport(String baseUrl, JwtInterceptor.TokenProvider tokenProvider) {
        OkHttpClient client = baseClientBuilder()
                .addInterceptor(new JwtInterceptor(tokenProvider))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(ReportApi.class);
    }

    // ✅ Create AlertApi with JWT auth
    public static AlertApi createAlert(JwtInterceptor.TokenProvider tokenProvider) {
        return createAlert(DEFAULT_BASE_URL, tokenProvider);
    }

    public static AlertApi createAlert(Context context, JwtInterceptor.TokenProvider tokenProvider) {
        return createAlert(getBaseUrl(context), tokenProvider);
    }

    public static AlertApi createAlert(String baseUrl, JwtInterceptor.TokenProvider tokenProvider) {
        OkHttpClient client = baseClientBuilder()
                .addInterceptor(new JwtInterceptor(tokenProvider))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(AlertApi.class);
    }

    // ✅ Create FollowApi with JWT auth
    public static FollowApi createFollow(JwtInterceptor.TokenProvider tokenProvider) {
        return createFollow(DEFAULT_BASE_URL, tokenProvider);
    }

    public static FollowApi createFollow(Context context, JwtInterceptor.TokenProvider tokenProvider) {
        return createFollow(getBaseUrl(context), tokenProvider);
    }

    public static FollowApi createFollow(String baseUrl, JwtInterceptor.TokenProvider tokenProvider) {
        OkHttpClient client = baseClientBuilder()
                .addInterceptor(new JwtInterceptor(tokenProvider))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl + "api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(FollowApi.class);
    }
}
