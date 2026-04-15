package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.smartwater.monitoring.adapter.UserAdapter;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.FollowApi;
import com.smartwater.monitoring.network.dto.PageResponse;
import com.smartwater.monitoring.network.dto.UserProfileResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for displaying followers/following lists
 */
public class FollowListActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_TYPE = "type"; // "followers" or "following"

    private Long userId;
    private String type;
    private String authToken;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvUsers;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;

    private FollowApi followApi;
    private List<UserProfileResponse> users = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        type = getIntent().getStringExtra(EXTRA_TYPE);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupApiClient();
        setupRecyclerView();
        setupTabLayout();

        // Select initial tab based on type
        if ("following".equals(type)) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        } else {
            loadFollowers();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        rvUsers = findViewById(R.id.rvUsers);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupApiClient() {
        SharedPreferences prefs = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        authToken = prefs.getString("jwt_token", "");
        followApi = ApiClient.createFollow(this, () -> authToken);
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this, users, user -> {
            // Open user profile
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(UserProfileActivity.EXTRA_USER_ID, user.getId());
            startActivity(intent);
        }, (user, isFollowing) -> {
            // Handle follow/unfollow
            if (isFollowing) {
                unfollowUser(user);
            } else {
                followUser(user);
            }
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadFollowers();
                } else {
                    loadFollowing();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFollowers() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        rvUsers.setVisibility(View.GONE);

        followApi.getFollowers(userId, null, null).enqueue(new Callback<PageResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<UserProfileResponse>> call, @NonNull Response<PageResponse<UserProfileResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    users.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();

                    if (users.isEmpty()) {
                        showEmptyState("No followers yet");
                    } else {
                        rvUsers.setVisibility(View.VISIBLE);
                    }
                } else {
                    showEmptyState("Failed to load followers");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState("Network error");
            }
        });
    }

    private void loadFollowing() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        rvUsers.setVisibility(View.GONE);

        followApi.getFollowing(userId, null, null).enqueue(new Callback<PageResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<UserProfileResponse>> call, @NonNull Response<PageResponse<UserProfileResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    users.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();

                    if (users.isEmpty()) {
                        showEmptyState("Not following anyone yet");
                    } else {
                        rvUsers.setVisibility(View.VISIBLE);
                    }
                } else {
                    showEmptyState("Failed to load following");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState("Network error");
            }
        });
    }

    private void followUser(UserProfileResponse user) {
        followApi.followUser(user.getId()).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    user.setIsFollowing(true);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(FollowListActivity.this, "Following " + user.getFirstName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(FollowListActivity.this, "Failed to follow", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unfollowUser(UserProfileResponse user) {
        followApi.unfollowUser(user.getId()).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    user.setIsFollowing(false);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(FollowListActivity.this, "Unfollowed " + user.getFirstName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(FollowListActivity.this, "Failed to unfollow", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState(String message) {
        llEmptyState.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
    }
}
