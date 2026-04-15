package com.smartwater.monitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.smartwater.monitoring.R;
import com.smartwater.monitoring.UserProfileActivity;
import com.smartwater.monitoring.network.ApiClient;
import com.smartwater.monitoring.network.CommunityApi;
import com.smartwater.monitoring.network.dto.CommunityPostResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adapter for displaying community posts (Twitter-style)
 */
public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(CommunityPostResponse post);
    }

    private final Context context;
    private final List<CommunityPostResponse> posts;
    private final OnPostClickListener clickListener;
    private CommunityApi communityApi;

    public CommunityPostAdapter(Context context, List<CommunityPostResponse> posts, OnPostClickListener listener) {
        this.context = context;
        this.posts = posts;
        this.clickListener = listener;
        setupApi();
    }

    private void setupApi() {
        SharedPreferences prefs = context.getSharedPreferences("SmartWaterPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");
        communityApi = ApiClient.createCommunity(context, () -> token);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPostResponse post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        // Header
        private LinearLayout llRetweetedBy;
        private TextView tvRetweetedBy;
        private View flProfilePic;
        private ImageView ivProfilePic;
        private ImageView ivExpertBadge;
        private LinearLayout llAuthorInfo;
        private TextView tvAuthorName;
        private ImageView ivVerifiedBadge;
        private TextView tvLocation;
        private TextView tvTimestamp;
        private TextView tvViewCount;

        // Content
        private TextView tvContent;
        private LinearLayout llSensorData;
        private TextView tvSensorData;
        private CardView cvPostImage;
        private ImageView ivPostImage;

        // Action buttons
        private LinearLayout llReplyButton;
        private ImageView ivReplyIcon;
        private TextView tvReplyCount;
        private LinearLayout llRetweetButton;
        private ImageView ivRetweetIcon;
        private TextView tvRetweetCount;
        private LinearLayout llLikeButton;
        private ImageView ivLikeIcon;
        private TextView tvLikeCount;
        private LinearLayout llBookmarkButton;
        private ImageView ivBookmarkIcon;
        private LinearLayout llShareButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Header
            llRetweetedBy = itemView.findViewById(R.id.llRetweetedBy);
            tvRetweetedBy = itemView.findViewById(R.id.tvRetweetedBy);
            flProfilePic = itemView.findViewById(R.id.flProfilePic);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            ivExpertBadge = itemView.findViewById(R.id.ivExpertBadge);
            llAuthorInfo = itemView.findViewById(R.id.llAuthorInfo);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            ivVerifiedBadge = itemView.findViewById(R.id.ivVerifiedBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvViewCount = itemView.findViewById(R.id.tvViewCount);

            // Content
            tvContent = itemView.findViewById(R.id.tvContent);
            llSensorData = itemView.findViewById(R.id.llSensorData);
            tvSensorData = itemView.findViewById(R.id.tvSensorData);
            cvPostImage = itemView.findViewById(R.id.cvPostImage);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);

            // Actions
            llReplyButton = itemView.findViewById(R.id.llReplyButton);
            ivReplyIcon = itemView.findViewById(R.id.ivReplyIcon);
            tvReplyCount = itemView.findViewById(R.id.tvReplyCount);
            llRetweetButton = itemView.findViewById(R.id.llRetweetButton);
            ivRetweetIcon = itemView.findViewById(R.id.ivRetweetIcon);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            llLikeButton = itemView.findViewById(R.id.llLikeButton);
            ivLikeIcon = itemView.findViewById(R.id.ivLikeIcon);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            llBookmarkButton = itemView.findViewById(R.id.llBookmarkButton);
            ivBookmarkIcon = itemView.findViewById(R.id.ivBookmarkIcon);
            llShareButton = itemView.findViewById(R.id.llShareButton);
        }

        void bind(CommunityPostResponse post) {
            // Set author info
            tvAuthorName.setText(post.getAuthorName() != null ? post.getAuthorName() : "Unknown");
            tvLocation.setText(post.getLocation() != null ? post.getLocation() : "Unknown Location");
            tvTimestamp.setText(formatTime(post.getCreatedAt()));
            tvContent.setText(post.getContent() != null ? post.getContent() : "");

            // View count
            if (tvViewCount != null) {
                int views = post.getViewCount() != null ? post.getViewCount() : 0;
                tvViewCount.setText(formatCount(views) + " views");
            }

            // Expert badge
            Boolean isExpert = post.getAuthorIsExpert();
            if (ivExpertBadge != null) {
                ivExpertBadge.setVisibility(Boolean.TRUE.equals(isExpert) ? View.VISIBLE : View.GONE);
            }
            if (ivVerifiedBadge != null) {
                ivVerifiedBadge.setVisibility(Boolean.TRUE.equals(isExpert) ? View.VISIBLE : View.GONE);
            }

            // Retweet header
            if (llRetweetedBy != null) {
                if (post.getIsRetweet() != null && post.getIsRetweet()) {
                    llRetweetedBy.setVisibility(View.VISIBLE);
                    String retweetedByName = post.getRetweetedByName();
                    tvRetweetedBy.setText((retweetedByName != null ? retweetedByName : "Someone") + " Retweeted");
                } else {
                    llRetweetedBy.setVisibility(View.GONE);
                }
            }

            // Sensor data
            if (post.getPh() != null || post.getTemperature() != null) {
                llSensorData.setVisibility(View.VISIBLE);
                StringBuilder sensorText = new StringBuilder();
                if (post.getPh() != null) {
                    sensorText.append("pH: ").append(String.format(Locale.getDefault(), "%.1f", post.getPh()));
                }
                if (post.getTemperature() != null) {
                    if (sensorText.length() > 0) sensorText.append(" | ");
                    sensorText.append("Temp: ").append(String.format(Locale.getDefault(), "%.1f°C", post.getTemperature()));
                }
                tvSensorData.setText(sensorText.toString());
            } else {
                llSensorData.setVisibility(View.GONE);
            }

            // Post image (if available)
            if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                cvPostImage.setVisibility(View.VISIBLE);
                // TODO: Load image with Glide/Picasso
            } else {
                cvPostImage.setVisibility(View.GONE);
            }

            // Engagement counts
            int replyCount = post.getReplyCount() != null ? post.getReplyCount() : 0;
            int likeCount = post.getLikeCount() != null ? post.getLikeCount() : (post.getLikes() != null ? post.getLikes() : 0);
            int retweetCount = post.getRetweetCount() != null ? post.getRetweetCount() : 0;

            tvReplyCount.setText(formatCount(replyCount));
            tvLikeCount.setText(formatCount(likeCount));
            if (tvRetweetCount != null) {
                tvRetweetCount.setText(formatCount(retweetCount));
            }

            // User interaction status
            updateLikeButton(post.getIsLikedByCurrentUser());
            updateRetweetButton(post.getIsRetweetedByCurrentUser());
            updateBookmarkButton(post.getIsBookmarkedByCurrentUser());

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onPostClick(post);
                }
            });

            // Profile click
            View.OnClickListener profileClick = v -> {
                if (post.getAuthorId() != null) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.EXTRA_USER_ID, post.getAuthorId());
                    context.startActivity(intent);
                }
            };
            if (flProfilePic != null) flProfilePic.setOnClickListener(profileClick);
            if (llAuthorInfo != null) llAuthorInfo.setOnClickListener(profileClick);

            // Reply click
            if (llReplyButton != null) {
                llReplyButton.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onPostClick(post);
                    }
                });
            }

            // Like click
            if (llLikeButton != null) {
                llLikeButton.setOnClickListener(v -> toggleLike(post, getAdapterPosition()));
            }

            // Retweet click
            if (llRetweetButton != null) {
                llRetweetButton.setOnClickListener(v -> toggleRetweet(post, getAdapterPosition()));
            }

            // Bookmark click
            if (llBookmarkButton != null) {
                llBookmarkButton.setOnClickListener(v -> toggleBookmark(post, getAdapterPosition()));
            }

            // Share click
            if (llShareButton != null) {
                llShareButton.setOnClickListener(v -> sharePost(post));
            }
        }

        private void updateLikeButton(Boolean isLiked) {
            if (ivLikeIcon != null) {
                if (Boolean.TRUE.equals(isLiked)) {
                    ivLikeIcon.setImageResource(android.R.drawable.star_big_on);
                    ivLikeIcon.setColorFilter(context.getColor(R.color.accent_pink));
                    tvLikeCount.setTextColor(context.getColor(R.color.accent_pink));
                } else {
                    ivLikeIcon.setImageResource(android.R.drawable.star_big_off);
                    ivLikeIcon.setColorFilter(context.getColor(R.color.text_secondary_dark));
                    tvLikeCount.setTextColor(context.getColor(R.color.text_secondary_dark));
                }
            }
        }

        private void updateRetweetButton(Boolean isRetweeted) {
            if (ivRetweetIcon != null && tvRetweetCount != null) {
                if (Boolean.TRUE.equals(isRetweeted)) {
                    ivRetweetIcon.setColorFilter(context.getColor(R.color.accent));
                    tvRetweetCount.setTextColor(context.getColor(R.color.accent));
                } else {
                    ivRetweetIcon.setColorFilter(context.getColor(R.color.text_secondary_dark));
                    tvRetweetCount.setTextColor(context.getColor(R.color.text_secondary_dark));
                }
            }
        }

        private void updateBookmarkButton(Boolean isBookmarked) {
            if (ivBookmarkIcon != null) {
                if (Boolean.TRUE.equals(isBookmarked)) {
                    ivBookmarkIcon.setImageResource(android.R.drawable.btn_star_big_on);
                    ivBookmarkIcon.setColorFilter(context.getColor(R.color.primary));
                } else {
                    ivBookmarkIcon.setImageResource(android.R.drawable.btn_star_big_off);
                    ivBookmarkIcon.setColorFilter(context.getColor(R.color.text_secondary_dark));
                }
            }
        }

        private void toggleLike(CommunityPostResponse post, int position) {
            communityApi.toggleLike(post.getId()).enqueue(new Callback<CommunityPostResponse>() {
                @Override
                public void onResponse(@NonNull Call<CommunityPostResponse> call, @NonNull Response<CommunityPostResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CommunityPostResponse updated = response.body();
                        post.setIsLikedByCurrentUser(updated.getIsLikedByCurrentUser());
                        post.setLikes(updated.getLikes());
                        post.setLikeCount(updated.getLikeCount());
                        notifyItemChanged(position);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CommunityPostResponse> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void toggleRetweet(CommunityPostResponse post, int position) {
            if (Boolean.TRUE.equals(post.getIsRetweetedByCurrentUser())) {
                // Undo retweet
                communityApi.undoRetweet(post.getId()).enqueue(new Callback<CommunityPostResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CommunityPostResponse> call, @NonNull Response<CommunityPostResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CommunityPostResponse updated = response.body();
                            post.setIsRetweetedByCurrentUser(updated.getIsRetweetedByCurrentUser());
                            post.setRetweetCount(updated.getRetweetCount());
                            notifyItemChanged(position);
                            Toast.makeText(context, "Retweet removed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CommunityPostResponse> call, @NonNull Throwable t) {
                        Toast.makeText(context, "Failed to undo retweet", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Retweet
                communityApi.retweet(post.getId()).enqueue(new Callback<CommunityPostResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CommunityPostResponse> call, @NonNull Response<CommunityPostResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CommunityPostResponse updated = response.body();
                            post.setIsRetweetedByCurrentUser(updated.getIsRetweetedByCurrentUser());
                            post.setRetweetCount(updated.getRetweetCount());
                            notifyItemChanged(position);
                            Toast.makeText(context, "Retweeted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CommunityPostResponse> call, @NonNull Throwable t) {
                        Toast.makeText(context, "Failed to retweet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void toggleBookmark(CommunityPostResponse post, int position) {
            communityApi.toggleBookmark(post.getId()).enqueue(new Callback<CommunityPostResponse>() {
                @Override
                public void onResponse(@NonNull Call<CommunityPostResponse> call, @NonNull Response<CommunityPostResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CommunityPostResponse updated = response.body();
                        post.setIsBookmarkedByCurrentUser(updated.getIsBookmarkedByCurrentUser());
                        post.setBookmarkCount(updated.getBookmarkCount());
                        notifyItemChanged(position);
                        String msg = Boolean.TRUE.equals(updated.getIsBookmarkedByCurrentUser()) ? "Bookmarked" : "Removed from bookmarks";
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CommunityPostResponse> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Failed to update bookmark", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void sharePost(CommunityPostResponse post) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = post.getContent() + "\n\n- Shared from SmartWater";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            context.startActivity(Intent.createChooser(shareIntent, "Share post via"));
        }
    }

    private String formatTime(String createdAt) {
        if (createdAt == null) return "";
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(createdAt);
            if (date == null) return createdAt;
            
            long diffMs = System.currentTimeMillis() - date.getTime();
            long diffMins = TimeUnit.MILLISECONDS.toMinutes(diffMs);
            long diffHours = TimeUnit.MILLISECONDS.toHours(diffMs);
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);
            
            if (diffMins < 1) return "Just now";
            if (diffMins < 60) return diffMins + "m";
            if (diffHours < 24) return diffHours + "h";
            if (diffDays < 7) return diffDays + "d";
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            return displayFormat.format(date);
        } catch (ParseException e) {
            return createdAt;
        }
    }

    private String formatCount(int count) {
        if (count >= 1000000) {
            return String.format(Locale.getDefault(), "%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format(Locale.getDefault(), "%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }
}
