package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.PageResponse;
import com.smartwater.monitoring.network.dto.UserProfileResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API interface for Follow operations (Twitter-like)
 */
public interface FollowApi {

    /**
     * Follow a user
     */
    @POST("users/{userId}/follow")
    Call<Map<String, Object>> followUser(@Path("userId") Long userId);

    /**
     * Unfollow a user
     */
    @DELETE("users/{userId}/follow")
    Call<Map<String, Object>> unfollowUser(@Path("userId") Long userId);

    /**
     * Check if current user follows target user
     */
    @GET("users/{userId}/is-following")
    Call<Map<String, Object>> isFollowing(@Path("userId") Long userId);

    /**
     * Get followers of a user
     */
    @GET("users/{userId}/followers")
    Call<PageResponse<UserProfileResponse>> getFollowers(
            @Path("userId") Long userId,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * Get users that a user is following
     */
    @GET("users/{userId}/following")
    Call<PageResponse<UserProfileResponse>> getFollowing(
            @Path("userId") Long userId,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * Get user profile by ID
     */
    @GET("users/{userId}/profile")
    Call<UserProfileResponse> getUserProfile(@Path("userId") Long userId);

    /**
     * Get current user's profile
     */
    @GET("users/me/profile")
    Call<UserProfileResponse> getMyProfile();
}
