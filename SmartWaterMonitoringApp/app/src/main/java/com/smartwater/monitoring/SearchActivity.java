package com.smartwater.monitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
 * Activity for searching posts
 */
public class SearchActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etSearch;
    private ImageView ivSearch;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;

    private CommunityApi communityApi;
    private List<CommunityPostResponse> posts = new ArrayList<>();
    private CommunityPostAdapter adapter;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupApiClient();
        setupRecyclerView();
        setupSearchListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etSearch = findViewById(R.id.etSearch);
        ivSearch = findViewById(R.id.ivSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        progressBar = findViewById(R.id.progressBar);

        ivBack.setOnClickListener(v -> onBackPressed());
        ivSearch.setOnClickListener(v -> performSearch());
    }

    private void setupApiClient() {
        SharedPreferences prefs = getSharedPreferences("SmartWaterPrefs", MODE_PRIVATE);
        authToken = prefs.getString("jwt_token", "");
        communityApi = ApiClient.createCommunity(this, () -> authToken);
    }

    private void setupRecyclerView() {
        adapter = new CommunityPostAdapter(this, posts, post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            startActivity(intent);
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);
    }

    private void setupSearchListeners() {
        // Handle enter key
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Focus and show keyboard
        etSearch.requestFocus();
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);

        communityApi.searchPosts(query, null, null).enqueue(new Callback<PageResponse<CommunityPostResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Response<PageResponse<CommunityPostResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    posts.clear();
                    posts.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();

                    if (posts.isEmpty()) {
                        showEmptyState("No results found", "Try different keywords");
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        rvSearchResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    showEmptyState("Search failed", "Please try again");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<CommunityPostResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState("Network error", "Check your connection");
            }
        });
    }

    private void showEmptyState(String title, String message) {
        llEmptyState.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        tvEmptyTitle.setText(title);
        tvEmptyMessage.setText(message);
    }
}
