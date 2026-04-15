package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.smartwater.monitoring.adapter.CommunityPostAdapter;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.CommunityApi;
import com.smartwater.monitoring.network.FollowApi;
import com.smartwater.monitoring.network.dto.CommunityPostResponse;
import com.smartwater.monitoring.network.dto.PageResponse;
import com.smartwater.monitoring.network.dto.UserProfileResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for viewing user profiles (Twitter-style)
 */
public class UserProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";

    private Long userId;
    private String authToken;
    private UserProfileResponse userProfile;

    // Views
    private Toolbar toolbar;
    private ImageView ivProfileImage;
    private ImageView ivExpertBadge;
    private ImageView ivVerifiedIcon;
    private TextView tvUserName;
    private TextView tvUserHandle;
    private TextView tvBio;
    private TextView tvJoinDate;
    private TextView tvFollowingCount;
    private TextView tvFollowerCount;
    private MaterialButton btnFollow;
    private LinearLayout llFollowing;
    private LinearLayout llFollowers;
    private TabLayout tabLayout;
    private RecyclerView rvUserPosts;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;

    // API clients
    private FollowApi followApi;
    private CommunityApi communityApi;

    // Posts list
    private List<CommunityPostResponse> posts = new ArrayList<>();
    private CommunityPostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupApiClients();
        setupRecyclerView();
        setupTabLayout();
        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivExpertBadge = findViewById(R.id.ivExpertBadge);
        ivVerifiedIcon = findViewById(R.id.ivVerifiedIcon);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserHandle = findViewById(R.id.tvUserHandle);
        tvBio = findViewById(R.id.tvBio);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvFollowerCount = findViewById(R.id.tvFollowerCount);
        btnFollow = findViewById(R.id.btnFollow);
        llFollowing = findViewById(R.id.llFollowing);
        llFollowers = findViewById(R.id.llFollowers);
        tabLayout = findViewById(R.id.tabLayout);
        rvUserPosts = findViewById(R.id.rvUserPosts);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        progressBar = findViewById(R.id.progressBar);

        btnFollow.setOnClickListener(v -> toggleFollow());
        llFollowing.setOnClickListener(v -> showFollowing());
        llFollowers.setOnClickListener(v -> showFollowers());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupApiClients() {
        SharedPreferences prefs = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        authToken = prefs.getString("jwt_token", "");

        followApi = ApiClient.createFollow(this, () -> authToken);
        communityApi = ApiClient.createCommunity(this, () -> authToken);
    }

    private void setupRecyclerView() {
        adapter = new CommunityPostAdapter(this, posts, post -> {
            // Handle post click
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            startActivity(intent);
        });

        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        rvUserPosts.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Posts
                        loadUserPosts();
                        break;
                    case 1: // Replies
                        // TODO: Load user replies
                        showEmptyState("No replies yet");
                        break;
                    case 2: // Likes
                        loadUserLikes();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        followApi.getUserProfile(userId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    userProfile = response.body();
                    displayProfile();
                    loadUserPosts();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProfile() {
        if (userProfile == null) return;

        // Set name
        tvUserName.setText(userProfile.getFullName());

        // Set handle (email-based)
        if (userProfile.getEmail() != null) {
            String handle = "@" + userProfile.getEmail().split("@")[0];
            tvUserHandle.setText(handle);
        }

        // Set bio
        if (userProfile.getBio() != null && !userProfile.getBio().isEmpty()) {
            tvBio.setText(userProfile.getBio());
            tvBio.setVisibility(View.VISIBLE);
        } else {
            tvBio.setVisibility(View.GONE);
        }

        // Set join date
        if (userProfile.getCreatedAt() != null) {
            try {
                // Assuming createdAt is in ISO format
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                tvJoinDate.setText("Joined " + sdf.format(new Date()));
            } catch (Exception e) {
                tvJoinDate.setText("Joined recently");
            }
        }

        // Set stats
        tvFollowingCount.setText(formatCount(userProfile.getFollowingCount()));
        tvFollowerCount.setText(formatCount(userProfile.getFollowerCount()));

        // Set expert badge
        if (userProfile.getIsExpert()) {
            ivExpertBadge.setVisibility(View.VISIBLE);
            ivVerifiedIcon.setVisibility(View.VISIBLE);
        } else {
            ivExpertBadge.setVisibility(View.GONE);
            ivVerifiedIcon.setVisibility(View.GONE);
        }

        // Set follow button state
        updateFollowButton(userProfile.getIsFollowing());

        // Hide follow button if viewing own profile
        SharedPreferences prefs = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        String currentUserEmail = prefs.getString("user_email", "");
        if (currentUserEmail.equals(userProfile.getEmail())) {
            btnFollow.setText("Edit Profile");
            btnFollow.setOnClickListener(v -> {
                // TODO: Open edit profile activity
                Toast.makeText(this, "Edit profile coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundTintList(getColorStateList(R.color.text_secondary_dark));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundTintList(getColorStateList(R.color.primary));
        }
    }

    private void toggleFollow() {
        if (userProfile == null) return;

        boolean isCurrentlyFollowing = userProfile.getIsFollowing();

        if (isCurrentlyFollowing) {
            // Unfollow
            followApi.unfollowUser(userId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        userProfile.setIsFollowing(false);
                        userProfile.setFollowerCount(Math.max(0, userProfile.getFollowerCount() - 1));
                        updateFollowButton(false);
                        tvFollowerCount.setText(formatCount(userProfile.getFollowerCount()));
                        Toast.makeText(UserProfileActivity.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                    Toast.makeText(UserProfileActivity.this, "Failed to unfollow", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Follow
            followApi.followUser(userId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        userProfile.setIsFollowing(true);
                        userProfile.setFollowerCount(userProfile.getFollowerCount() + 1);
                        updateFollowButton(true);
                        tvFollowerCount.setText(formatCount(userProfile.getFollowerCount()));
                        Toast.makeText(UserProfileActivity.this, "Following", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                    Toast.makeText(UserProfileActivity.this, "Failed to follow", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUserPosts() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        communityApi.getPostsByUser(userId, null, null).enqueue(new Callback<PageResponse<CommunityPostResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Response<PageResponse<CommunityPostResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    posts.clear();
                    posts.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();

                    if (posts.isEmpty()) {
                        showEmptyState("No posts yet");
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvUserPosts.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState("Failed to load posts");
            }
        });
    }

    private void loadUserLikes() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        // Only load likes if viewing own profile
        SharedPreferences prefs = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        String currentUserEmail = prefs.getString("user_email", "");
        
        if (userProfile != null && currentUserEmail.equals(userProfile.getEmail())) {
            communityApi.getMyLikedPosts(null, null).enqueue(new Callback<PageResponse<CommunityPostResponse>>() {
                @Override
                public void onResponse(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Response<PageResponse<CommunityPostResponse>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        posts.clear();
                        posts.addAll(response.body().getItems());
                        adapter.notifyDataSetChanged();

                        if (posts.isEmpty()) {
                            showEmptyState("No liked posts yet");
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                            rvUserPosts.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showEmptyState("Failed to load liked posts");
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            showEmptyState("Likes are private");
        }
    }

    private void showFollowing() {
        Intent intent = new Intent(this, FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId);
        intent.putExtra(FollowListActivity.EXTRA_TYPE, "following");
        startActivity(intent);
    }

    private void showFollowers() {
        Intent intent = new Intent(this, FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXTRA_USER_ID, userId);
        intent.putExtra(FollowListActivity.EXTRA_TYPE, "followers");
        startActivity(intent);
    }

    private void showEmptyState(String message) {
        llEmptyState.setVisibility(View.VISIBLE);
        rvUserPosts.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
    }

    private String formatCount(Integer count) {
        if (count == null) return "0";
        if (count >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format(Locale.getDefault(), "%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }
}
