package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartwater.monitoring.adapter.CommunityPostAdapter;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.CommunityApi;
import com.smartwater.monitoring.network.dto.CommunityPostResponse;
import com.smartwater.monitoring.network.dto.PageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for displaying bookmarked posts
 */
public class BookmarksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvBookmarks;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;

    private CommunityApi communityApi;
    private List<CommunityPostResponse> bookmarks = new ArrayList<>();
    private CommunityPostAdapter adapter;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        initViews();
        setupToolbar();
        setupApiClient();
        setupRecyclerView();
        loadBookmarks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookmarks when returning
        loadBookmarks();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvBookmarks = findViewById(R.id.rvBookmarks);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bookmarks");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupApiClient() {
        SharedPreferences prefs = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        authToken = prefs.getString("jwt_token", "");
        communityApi = ApiClient.createCommunity(this, () -> authToken);
    }

    private void setupRecyclerView() {
        adapter = new CommunityPostAdapter(this, bookmarks, post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            startActivity(intent);
        });

        rvBookmarks.setLayoutManager(new LinearLayoutManager(this));
        rvBookmarks.setAdapter(adapter);
    }

    private void loadBookmarks() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        rvBookmarks.setVisibility(View.GONE);

        communityApi.getMyBookmarks(null, null).enqueue(new Callback<PageResponse<CommunityPostResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Response<PageResponse<CommunityPostResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    bookmarks.clear();
                    bookmarks.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();

                    if (bookmarks.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        rvBookmarks.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvBookmarks.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(BookmarksActivity.this, "Failed to load bookmarks", Toast.LENGTH_SHORT).show();
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookmarksActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                llEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
