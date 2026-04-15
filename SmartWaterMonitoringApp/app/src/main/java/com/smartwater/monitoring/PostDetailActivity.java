package com.smartwater.monitoring;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.CommunityApi;
import com.smartwater.monitoring.network.TokenStore;
import com.smartwater.monitoring.network.dto.CommunityReplyRequest;
import com.smartwater.monitoring.network.dto.CommunityReplyResponse;
import com.smartwater.monitoring.network.dto.PageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PostDetailActivity shows a single post with all its replies
 * Users can view and add replies in real-time
 */
public class PostDetailActivity extends AppCompatActivity {

    // Post data passed from CommunityActivity
    private Long postId;
    private String authorName;
    private String content;
    private String location;
    private String timestamp;
    private String sensorData;
    private int replyCount;
    private int likeCount;

    // UI Components
    private ImageButton btnBack, btnSendReply;
    private TextView tvAuthorName, tvLocation, tvTimestamp, tvContent;
    private TextView tvSensorData, tvReplyCount, tvLikeCount, tvNoReplies;
    private LinearLayout llSensorData, llRepliesContainer;
    private EditText etReplyInput;

    // Backend API
    private CommunityApi communityApi;
    private TokenStore tokenStore;

    // Auto-refresh handler
    private android.os.Handler refreshHandler;
    private static final int REFRESH_INTERVAL = 10000; // 10 seconds

    // Track local replies in demo mode (so they don't disappear)
    private java.util.ArrayList<String[]> localReplies = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get data from intent
        postId = getIntent().getLongExtra("postId", -1);
        authorName = getIntent().getStringExtra("authorName");
        content = getIntent().getStringExtra("content");
        location = getIntent().getStringExtra("location");
        timestamp = getIntent().getStringExtra("timestamp");
        sensorData = getIntent().getStringExtra("sensorData");
        replyCount = getIntent().getIntExtra("replyCount", 0);
        likeCount = getIntent().getIntExtra("likeCount", 0);

        // Initialize API
        tokenStore = new TokenStore(this);
        communityApi = ApiClient.createCommunity(this, () -> tokenStore.getToken());

        // Initialize views
        initializeViews();

        // Display post data
        displayPostData();

        // Load replies
        loadReplies();

        // Set click listeners
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only auto-refresh for real backend posts, not demo mode
        if (postId != null && postId != -1) {
            startAutoRefresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSendReply = findViewById(R.id.btnSendReply);
        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvLocation = findViewById(R.id.tvLocation);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvContent = findViewById(R.id.tvContent);
        tvSensorData = findViewById(R.id.tvSensorData);
        tvReplyCount = findViewById(R.id.tvReplyCount);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvNoReplies = findViewById(R.id.tvNoReplies);
        llSensorData = findViewById(R.id.llSensorData);
        llRepliesContainer = findViewById(R.id.llRepliesContainer);
        etReplyInput = findViewById(R.id.etReplyInput);
    }

    private void displayPostData() {
        tvAuthorName.setText(authorName != null ? authorName : "Anonymous");
        tvLocation.setText(location != null ? location : "Unknown");
        tvTimestamp.setText(timestamp != null ? timestamp : "Just now");
        tvContent.setText(content != null ? content : "");
        tvReplyCount.setText("💬 " + replyCount + " Replies");
        tvLikeCount.setText("⭐ " + likeCount);

        if (sensorData != null && !sensorData.isEmpty()) {
            llSensorData.setVisibility(View.VISIBLE);
            tvSensorData.setText(sensorData);
        } else {
            llSensorData.setVisibility(View.GONE);
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSendReply.setOnClickListener(v -> {
            String replyText = etReplyInput.getText().toString().trim();
            if (replyText.isEmpty()) {
                Toast.makeText(this, "Please enter a reply", Toast.LENGTH_SHORT).show();
                return;
            }
            sendReply(replyText);
        });
    }

    private void loadReplies() {
        if (postId == null || postId == -1) {
            // Sample post - show demo replies
            addDemoReplies();
            return;
        }

        communityApi.getReplies(postId, null, null).enqueue(new Callback<PageResponse<CommunityReplyResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<CommunityReplyResponse>> call,
                                   @NonNull Response<PageResponse<CommunityReplyResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                    llRepliesContainer.removeAllViews();
                    
                    if (response.body().getContent().isEmpty()) {
                        tvNoReplies.setVisibility(View.VISIBLE);
                    } else {
                        tvNoReplies.setVisibility(View.GONE);
                        for (CommunityReplyResponse reply : response.body().getContent()) {
                            addReplyToUI(reply.getAuthorName(), reply.getContent(), 
                                       reply.getCreatedAt(), reply.getExpertReply());
                        }
                    }
                    
                    // Update reply count
                    replyCount = response.body().getContent().size();
                    tvReplyCount.setText("💬 " + replyCount + " Replies");
                } else {
                    addDemoReplies();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<CommunityReplyResponse>> call, @NonNull Throwable t) {
                addDemoReplies();
            }
        });
    }

    private void addDemoReplies() {
        llRepliesContainer.removeAllViews();
        addReplyToUI("Sarah Lee", "Great observation! I noticed the same thing yesterday. 👍", "1h", false);
        addReplyToUI("Dr. Ahmad", "The pH level looks optimal for aquatic life. Keep monitoring!", "45m", true);
        addReplyToUI("Mike", "Thanks for sharing! Very helpful for our community. 🌊", "30m", false);
        
        // Re-add any local replies that user posted in demo mode
        for (String[] localReply : localReplies) {
            addReplyToUI(localReply[0], localReply[1], localReply[2], false);
        }
        
        tvNoReplies.setVisibility(View.GONE);
    }

    private void addReplyToUI(String author, String content, String time, Boolean isExpert) {
        View replyView = LayoutInflater.from(this).inflate(R.layout.item_reply, llRepliesContainer, false);

        TextView tvAuthor = replyView.findViewById(R.id.tvReplyAuthor);
        TextView tvContent = replyView.findViewById(R.id.tvReplyContent);
        TextView tvTime = replyView.findViewById(R.id.tvReplyTime);
        TextView tvExpertBadge = replyView.findViewById(R.id.tvExpertBadge);

        tvAuthor.setText(author != null ? author : "Anonymous");
        tvContent.setText(content != null ? content : "");
        tvTime.setText(time != null ? time : "Just now");

        if (Boolean.TRUE.equals(isExpert)) {
            tvExpertBadge.setVisibility(View.VISIBLE);
        } else {
            tvExpertBadge.setVisibility(View.GONE);
        }

        // Add divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(getResources().getColor(R.color.divider));

        llRepliesContainer.addView(replyView);
        llRepliesContainer.addView(divider);
    }

    private void sendReply(String replyText) {
        if (postId == null || postId == -1) {
            // Demo mode - add locally and track it
            localReplies.add(new String[]{"You", replyText, "Just now"});
            addReplyToUI("You", replyText, "Just now", false);
            etReplyInput.setText("");
            replyCount++;
            tvReplyCount.setText("💬 " + replyCount + " Replies");
            Toast.makeText(this, "💬 Reply posted! (Demo mode)", Toast.LENGTH_SHORT).show();
            return;
        }

        CommunityReplyRequest request = new CommunityReplyRequest(replyText, false);
        
        communityApi.addReply(postId, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    etReplyInput.setText("");
                    Toast.makeText(PostDetailActivity.this, 
                        "💬 Reply posted! Others will see it in real-time.", 
                        Toast.LENGTH_SHORT).show();
                    loadReplies(); // Refresh to show new reply
                } else {
                    Toast.makeText(PostDetailActivity.this, 
                        "Failed to post reply: " + response.code(), 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Toast.makeText(PostDetailActivity.this, 
                    "Network error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startAutoRefresh() {
        if (refreshHandler == null) {
            refreshHandler = new android.os.Handler();
        }
        refreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadReplies();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null) {
            refreshHandler.removeCallbacksAndMessages(null);
        }
    }
}
