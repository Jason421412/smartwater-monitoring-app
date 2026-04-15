package com.smartwater.monitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.CommunityApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.CommunityPostResponse;
import com.smartwater.monitoring.network.dto.PageResponse;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * CommunityActivity displays community posts about water quality
 * Users can view posts, replies, and create new posts
 */
public class CommunityActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnProfileIcon, btnLogoutIcon;
    private ListView lvCommunityPosts;
    private BottomNavigationView bottomNavigation;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabNewPost;

    // Navigation Helper
    private NavigationHelper navigationHelper;

    // Data
    private ArrayList<Post> postList;
    private PostAdapter postAdapter;
    private SharedPreferences sharedPreferences;

    // Backend API
    private CommunityApi communityApi;
    private TokenStore tokenStore;

    // Auto-refresh for real-time comments
    private android.os.Handler refreshHandler;
    private static final int REFRESH_INTERVAL = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Initialize navigation helper
        navigationHelper = new NavigationHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);

        // Initialize Backend API
        tokenStore = new TokenStore(this);
        communityApi = ApiClient.createCommunity(this, () -> tokenStore.getToken());

        // Initialize views
        initializeViews();

        // Load posts from backend
        loadPostsFromBackend();

        // Set click listeners
        setClickListeners();

        // Setup navigation
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload posts when returning
        loadPostsFromBackend();
        // Start auto-refresh for real-time comments
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refresh when leaving
        stopAutoRefresh();
    }

    /**
     * Start auto-refresh timer for real-time updates
     */
    private void startAutoRefresh() {
        if (refreshHandler == null) {
            refreshHandler = new android.os.Handler();
        }
        refreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadPostsFromBackend();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    /**
     * Stop auto-refresh timer
     */
    private void stopAutoRefresh() {
        if (refreshHandler != null) {
            refreshHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnProfileIcon = findViewById(R.id.btnProfileIcon);
        btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        lvCommunityPosts = findViewById(R.id.lvCommunityPosts);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabNewPost = findViewById(R.id.fabNewPost);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        lvCommunityPosts.setAdapter(postAdapter);

        // Setup pull-to-refresh
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            loadPostsFromBackend();
        });
    }

    /**
     * Set click listeners
     */
    private void setClickListeners() {
        // Floating action button for new post
        fabNewPost.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Setup navigation
     */
    private void setupNavigation() {
        navigationHelper.setupTopBar(btnProfileIcon, btnLogoutIcon);
        navigationHelper.setupBottomNavigation(bottomNavigation, R.id.navigation_community);
    }

    /**
     * Load posts from backend API
     */
    private void loadPostsFromBackend() {
        communityApi.getPosts(null, null, null).enqueue(new Callback<PageResponse<CommunityPostResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<CommunityPostResponse>> call,
                                 @NonNull Response<PageResponse<CommunityPostResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getContent() == null) {
                    android.widget.Toast.makeText(CommunityActivity.this,
                            "Failed to load posts: " + response.code(),
                            android.widget.Toast.LENGTH_SHORT).show();
                    // Show sample posts as fallback
                    postList.clear();
                    addSamplePosts();
                    postAdapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    return;
                }

                postList.clear();
                for (CommunityPostResponse postResp : response.body().getContent()) {
                    String sensorData = "";
                    if (postResp.getPh() != null || postResp.getTemperature() != null) {
                        sensorData = String.format("pH: %.2f | Temp: %.1f°C",
                                postResp.getPh() != null ? postResp.getPh() : 0.0,
                                postResp.getTemperature() != null ? postResp.getTemperature() : 0.0);
                    }

                    Post post = new Post(
                            postResp.getId(),  // postId for API calls
                            postResp.getAuthorName() != null ? postResp.getAuthorName() : "Anonymous",
                            postResp.getContent(),
                            postResp.getLocation() != null ? postResp.getLocation() : "Unknown",
                            postResp.getCreatedAt() != null ? postResp.getCreatedAt() : "Just now",
                            sensorData,
                            "", // imageUrl
                            postResp.getReplyCount() != null ? postResp.getReplyCount() : 0,
                            postResp.getLikeCount() != null ? postResp.getLikeCount() : 0,
                            postResp.getIsLikedByCurrentUser() != null ? postResp.getIsLikedByCurrentUser() : false
                    );
                    postList.add(post);
                }

                if (postList.isEmpty()) {
                    android.widget.Toast.makeText(CommunityActivity.this,
                            "No community posts yet. Be the first to post!",
                            android.widget.Toast.LENGTH_SHORT).show();
                }

                postAdapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<CommunityPostResponse>> call,
                                @NonNull Throwable t) {
                android.widget.Toast.makeText(CommunityActivity.this,
                        "Network error: " + t.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
                // Show sample posts as fallback
                postList.clear();
                addSamplePosts();
                postAdapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    /**
     * Load posts from SharedPreferences (deprecated - keeping for backwards compatibility)
     */
    private void loadPosts() {
        postList.clear();

        try {
            String postsJson = sharedPreferences.getString("communityPosts", "[]");
            JSONArray jsonArray = new JSONArray(postsJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Post post = new Post(
                        null,  // No postId from legacy JSON
                        jsonObject.getString("username"),
                        jsonObject.getString("content"),
                        jsonObject.getString("location"),
                        jsonObject.getString("timestamp"),
                        jsonObject.optString("sensorData", ""),
                        "", // ImageUrl not stored in legacy JSON
                        jsonObject.optInt("replyCount", 0),
                        jsonObject.optInt("likeCount", 0),
                        false
                );
                postList.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add sample posts if empty
        if (postList.isEmpty()) {
            addSamplePosts();
        }

        postAdapter.notifyDataSetChanged();
    }

    /**
     * Add sample posts for demonstration
     */
    private void addSamplePosts() {
        postList.add(new Post(
                null, // No postId for sample posts
                "John Doe",
                "🌊 Water quality seems great today at the riverside! Clear, fresh, and no unusual smell. Perfect day for a picnic! #WaterQuality",
                "Riverside Park, KL",
                "2h",
                "pH: 7.2 | Temp: 25.5\u00b0C",
                "",
                8,
                24,   // likeCount
                false // isLiked
        ));

        postList.add(new Post(
                null,
                "Jane Smith",
                "\u26a0\ufe0f Alert! Noticed some discoloration in the lake water this morning. The water has a slight greenish tint. Should we be concerned? Anyone else seeing this?",
                "Central Lake",
                "5h",
                "pH: 6.8 | Temp: 26.0\u00b0C",
                "",
                15,
                45,   // likeCount
                false // isLiked
        ));

        postList.add(new Post(
                null,
                "Mike Johnson",
                "\u2728 Great news everyone! The creek water quality has improved significantly after last week's cleanup campaign. Thanks to all volunteers! \ud83d\ude4c",
                "West Creek",
                "1d",
                "",
                "",
                23,
                89,   // likeCount
                false // isLiked
        ));

        postList.add(new Post(
                null,
                "Sarah Lee",
                "\ud83d\udcf8 Check out the crystal clear water at the beach today! Tested the pH and it's perfect. Nature is healing! \ud83c\udf0a\u2600\ufe0f",
                "Pantai Cenang",
                "3h",
                "pH: 7.5 | Temp: 28.0\u00b0C",
                "",
                42,
                156,  // likeCount
                false // isLiked
        ));

        postList.add(new Post(
                null,
                "Dr. Ahmad (Expert)",
                "\ud83d\udcca Weekly water quality report: Most areas showing excellent pH levels. Keep monitoring TDS in industrial zones. Stay vigilant, community! \ud83d\udca7",
                "Water Quality Lab",
                "6h",
                "",
                "",
                67,
                234,  // likeCount
                false // isLiked
        ));
    }

    /**
     * Show reply dialog
     */
    private void showReplyDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reply to Post");

        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Enter your reply...");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Post Reply", (dialog, which) -> {
            String replyText = input.getText().toString().trim();
            if (!replyText.isEmpty()) {
                Post post = postList.get(position);
                // Send reply to backend for real-time sync
                postReplyToBackend(post.postId, replyText, position);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Post reply to backend for real-time sync
     */
    private void postReplyToBackend(Long postId, String content, int position) {
        // For sample posts (null postId), save locally for demo
        if (postId == null) {
            Post post = postList.get(position);
            post.replyCount++;
            postAdapter.notifyDataSetChanged();
            android.widget.Toast.makeText(this, 
                "💬 Reply posted locally! (Demo mode - not synced to server)", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        com.smartwater.monitoring.network.dto.CommunityReplyRequest request = 
            new com.smartwater.monitoring.network.dto.CommunityReplyRequest(content, false);

        communityApi.addReply(postId, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    Post post = postList.get(position);
                    post.replyCount++;
                    postAdapter.notifyDataSetChanged();
                    android.widget.Toast.makeText(CommunityActivity.this, 
                        "💬 Reply posted! Others will see it in real-time.", 
                        android.widget.Toast.LENGTH_SHORT).show();
                    // Refresh to show latest
                    loadPostsFromBackend();
                } else {
                    android.widget.Toast.makeText(CommunityActivity.this, 
                        "Failed to post reply: " + response.code(), 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                android.widget.Toast.makeText(CommunityActivity.this, 
                    "Network error: " + t.getMessage(), 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Custom adapter for displaying posts in Twitter-style
     */
    private class PostAdapter extends ArrayAdapter<Post> {
        PostAdapter(Context context, ArrayList<Post> posts) {
            super(context, 0, posts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Post post = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_community_post, parent, false);
            }

            // Find views
            ImageView ivProfilePic = convertView.findViewById(R.id.ivProfilePic);
            TextView tvAuthorName = convertView.findViewById(R.id.tvAuthorName);
            TextView tvLocation = convertView.findViewById(R.id.tvLocation);
            TextView tvTimestamp = convertView.findViewById(R.id.tvTimestamp);
            TextView tvContent = convertView.findViewById(R.id.tvContent);
            LinearLayout llSensorData = convertView.findViewById(R.id.llSensorData);
            TextView tvSensorData = convertView.findViewById(R.id.tvSensorData);
            androidx.cardview.widget.CardView cvPostImage = convertView.findViewById(R.id.cvPostImage);
            ImageView ivPostImage = convertView.findViewById(R.id.ivPostImage);
            TextView tvReplyCount = convertView.findViewById(R.id.tvReplyCount);
            LinearLayout llLikeButton = convertView.findViewById(R.id.llLikeButton);
            ImageView ivLikeIcon = convertView.findViewById(R.id.ivLikeIcon);
            TextView tvLikeCount = convertView.findViewById(R.id.tvLikeCount);

            if (post != null) {
                // Set author name
                tvAuthorName.setText(post.authorName);
                
                // Set dynamic profile avatar color based on username
                int[] avatarColors = {
                    0xFF1DA1F2, // Twitter blue
                    0xFF17BF63, // Green
                    0xFFF45D22, // Orange
                    0xFF794BC4, // Purple
                    0xFFE0245E, // Red
                    0xFF1C9CEA, // Light blue
                    0xFF00BA7C, // Teal
                    0xFFFFAD1F  // Yellow
                };
                int colorIndex = Math.abs(post.authorName.hashCode()) % avatarColors.length;
                ivProfilePic.setColorFilter(avatarColors[colorIndex]);
                
                // Set location
                tvLocation.setText(post.location);
                
                // Set timestamp
                tvTimestamp.setText(post.timestamp);
                
                // Set content with hashtag highlighting
                String content = post.content != null ? post.content : "";
                android.text.SpannableString spannableContent = new android.text.SpannableString(content);
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#\\w+").matcher(content);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    // Blue color for hashtags
                    spannableContent.setSpan(
                        new android.text.style.ForegroundColorSpan(0xFF1DA1F2),
                        start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    // Make it bold
                    spannableContent.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
                tvContent.setText(spannableContent);

                // Add slide-in animation for new items
                android.view.animation.Animation slideIn = android.view.animation.AnimationUtils.loadAnimation(
                    getContext(), R.anim.item_slide_in);
                convertView.startAnimation(slideIn);

                // Show sensor data if available
                if (post.sensorData != null && !post.sensorData.isEmpty()) {
                    llSensorData.setVisibility(View.VISIBLE);
                    tvSensorData.setText(post.sensorData);
                } else {
                    llSensorData.setVisibility(View.GONE);
                }

                // Show post image if available (placeholder for now)
                if (post.imageUrl != null && !post.imageUrl.isEmpty()) {
                    cvPostImage.setVisibility(View.VISIBLE);
                    // TODO: Load actual image using Glide/Picasso when imageUrl is provided
                    // For now showing placeholder
                } else {
                    cvPostImage.setVisibility(View.GONE);
                }

                // Set reply count
                tvReplyCount.setText(post.replyCount > 0 ? post.replyCount + " replies" : "Reply");

                // Reply button click handler
                LinearLayout llReplyButton = convertView.findViewById(R.id.llReplyButton);
                final int pos = position;
                llReplyButton.setOnClickListener(v -> showReplyDialog(pos));

                // Set like count from backend data (NOT random!)
                tvLikeCount.setText(String.valueOf(post.likeCount));

                // Set initial like state from backend
                if (post.isLiked) {
                    ivLikeIcon.setImageResource(android.R.drawable.btn_star_big_on);
                    ivLikeIcon.setColorFilter(android.graphics.Color.parseColor("#E0245E")); // Twitter heart red
                } else {
                    ivLikeIcon.setImageResource(android.R.drawable.btn_star_big_off);
                    ivLikeIcon.setColorFilter(android.graphics.Color.parseColor("#657786")); // Twitter gray
                }

                // Like button interaction - call backend API
                final Post currentPost = post;
                llLikeButton.setOnClickListener(v -> {
                    // Play heart pop animation
                    android.view.animation.Animation popAnim = android.view.animation.AnimationUtils.loadAnimation(
                        getContext(), R.anim.like_pop);
                    ivLikeIcon.startAnimation(popAnim);

                    // For sample posts (null postId), toggle locally
                    if (currentPost.postId == null) {
                        currentPost.isLiked = !currentPost.isLiked;
                        currentPost.likeCount += currentPost.isLiked ? 1 : -1;
                        notifyDataSetChanged();
                        return;
                    }

                    // Call backend API to toggle like
                    communityApi.toggleLike(currentPost.postId).enqueue(new retrofit2.Callback<CommunityPostResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<CommunityPostResponse> call, retrofit2.Response<CommunityPostResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                currentPost.likeCount = response.body().getLikeCount() != null ? response.body().getLikeCount() : currentPost.likeCount;
                                currentPost.isLiked = response.body().getIsLikedByCurrentUser() != null ? response.body().getIsLikedByCurrentUser() : !currentPost.isLiked;
                                notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<CommunityPostResponse> call, Throwable t) {
                            // Toggle locally on failure for better UX
                            currentPost.isLiked = !currentPost.isLiked;
                            currentPost.likeCount += currentPost.isLiked ? 1 : -1;
                            notifyDataSetChanged();
                        }
                    });
                });

                // Click on post card to view detail with all replies
                final Post finalPost = post;
                final int finalLikeCount2 = post.likeCount;
                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(CommunityActivity.this, PostDetailActivity.class);
                    intent.putExtra("postId", finalPost.postId != null ? finalPost.postId : -1L);
                    intent.putExtra("authorName", finalPost.authorName);
                    intent.putExtra("content", finalPost.content);
                    intent.putExtra("location", finalPost.location);
                    intent.putExtra("timestamp", finalPost.timestamp);
                    intent.putExtra("sensorData", finalPost.sensorData);
                    intent.putExtra("replyCount", finalPost.replyCount);
                    intent.putExtra("likeCount", finalLikeCount2);
                    startActivity(intent);
                });
            }

            return convertView;
        }
    }

    /**
     * Post data class with image and postId support
     */
    private static class Post {
        Long postId;  // Backend ID for API calls
        String authorName;
        String content;
        String location;
        String timestamp;
        String sensorData;
        String imageUrl;
        int replyCount;
        int likeCount;    // ✅ Added: like count from backend
        boolean isLiked;  // ✅ Added: current user's like state

        Post(Long postId, String authorName, String content, String location, String timestamp, 
             String sensorData, String imageUrl, int replyCount, int likeCount, boolean isLiked) {
            this.postId = postId;
            this.authorName = authorName;
            this.content = content;
            this.location = location;
            this.timestamp = timestamp;
            this.sensorData = sensorData;
            this.imageUrl = imageUrl;
            this.replyCount = replyCount;
            this.likeCount = likeCount;
            this.isLiked = isLiked;
        }
    }
}
