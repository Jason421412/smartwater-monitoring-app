package com.smartwater.monitoring.network;

import com.smartwater.monitoring.network.dto.CommunityPostRequest;
import com.smartwater.monitoring.network.dto.CommunityPostResponse;
import com.smartwater.monitoring.network.dto.CommunityReplyRequest;
import com.smartwater.monitoring.network.dto.CommunityReplyResponse;
import com.smartwater.monitoring.network.dto.PageResponse;
import com.smartwater.monitoring.network.dto.QuoteTweetRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API interface for Community endpoints (Twitter-style)
 */
public interface CommunityApi {

    // ==================== POSTS ====================

    /**
     * Get all community posts
     */
    @GET("community/posts")
    Call<PageResponse<CommunityPostResponse>> getPosts(
            @Query("location") String location,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * Get post by ID
     */
    @GET("community/posts/{id}")
    Call<CommunityPostResponse> getPostById(@Path("id") Long id);

    /**
     * Create a new community post
     */
    @POST("community/posts")
    Call<CommunityPostResponse> createPost(@Body CommunityPostRequest request);

    /**
     * Delete a post
     */
    @DELETE("community/posts/{postId}")
    Call<Void> deletePost(@Path("postId") Long postId);

    // ==================== TWITTER-LIKE ENGAGEMENT ====================

    /**
     * Toggle like on a post (like/unlike)
     */
    @POST("community/posts/{postId}/like")
    Call<CommunityPostResponse> toggleLike(@Path("postId") Long postId);

    /**
     * Toggle bookmark on a post
     */
    @POST("community/posts/{postId}/bookmark")
    Call<CommunityPostResponse> toggleBookmark(@Path("postId") Long postId);

    /**
     * Retweet a post
     */
    @POST("community/posts/{postId}/retweet")
    Call<CommunityPostResponse> retweet(@Path("postId") Long postId);

    /**
     * Undo retweet
     */
    @DELETE("community/posts/{postId}/retweet")
    Call<CommunityPostResponse> undoRetweet(@Path("postId") Long postId);

    /**
     * Quote tweet
     */
    @POST("community/posts/{postId}/quote")
    Call<CommunityPostResponse> quoteTweet(@Path("postId") Long postId, @Body QuoteTweetRequest request);

    // ==================== USER CONTENT ====================

    /**
     * Get current user's bookmarks
     */
    @GET("community/bookmarks")
    Call<PageResponse<CommunityPostResponse>> getMyBookmarks(
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * Get current user's liked posts
     */
    @GET("community/likes")
    Call<PageResponse<CommunityPostResponse>> getMyLikedPosts(
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * Get posts by user ID
     */
    @GET("community/users/{userId}/posts")
    Call<PageResponse<CommunityPostResponse>> getPostsByUser(
            @Path("userId") Long userId,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    // ==================== SEARCH ====================

    /**
     * Search posts by query
     */
    @GET("community/search")
    Call<PageResponse<CommunityPostResponse>> searchPosts(
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    // ==================== REPLIES ====================

    /**
     * Add reply to a post
     */
    @POST("community/posts/{postId}/replies")
    Call<Object> addReply(@Path("postId") Long postId, @Body CommunityReplyRequest request);

    /**
     * Get all replies for a post
     */
    @GET("community/posts/{postId}/replies")
    Call<PageResponse<CommunityReplyResponse>> getReplies(
            @Path("postId") Long postId,
            @Query("page") Integer page,
            @Query("size") Integer size
    );
}
