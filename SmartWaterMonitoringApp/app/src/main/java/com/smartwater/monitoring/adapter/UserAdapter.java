package com.smartwater.monitoring.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartwater.monitoring.R;
import com.smartwater.monitoring.network.dto.UserProfileResponse;

import java.util.List;

/**
 * Adapter for displaying user list (followers/following)
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UserProfileResponse user);
    }

    public interface OnFollowClickListener {
        void onFollowClick(UserProfileResponse user, boolean isCurrentlyFollowing);
    }

    private final Context context;
    private final List<UserProfileResponse> users;
    private final OnUserClickListener userClickListener;
    private final OnFollowClickListener followClickListener;

    public UserAdapter(Context context, List<UserProfileResponse> users,
                       OnUserClickListener userClickListener,
                       OnFollowClickListener followClickListener) {
        this.context = context;
        this.users = users;
        this.userClickListener = userClickListener;
        this.followClickListener = followClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_follow, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfileResponse user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProfilePic;
        private final ImageView ivExpertBadge;
        private final ImageView ivVerifiedBadge;
        private final TextView tvUserName;
        private final TextView tvUserHandle;
        private final TextView tvBio;
        private final MaterialButton btnFollow;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            ivExpertBadge = itemView.findViewById(R.id.ivExpertBadge);
            ivVerifiedBadge = itemView.findViewById(R.id.ivVerifiedBadge);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserHandle = itemView.findViewById(R.id.tvUserHandle);
            tvBio = itemView.findViewById(R.id.tvBio);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }

        void bind(UserProfileResponse user) {
            // Set name
            tvUserName.setText(user.getFullName());

            // Set handle
            if (user.getEmail() != null) {
                String handle = "@" + user.getEmail().split("@")[0];
                tvUserHandle.setText(handle);
            }

            // Set bio
            if (user.getBio() != null && !user.getBio().isEmpty()) {
                tvBio.setText(user.getBio());
                tvBio.setVisibility(View.VISIBLE);
            } else {
                tvBio.setVisibility(View.GONE);
            }

            // Set expert badge
            if (user.getIsExpert()) {
                ivExpertBadge.setVisibility(View.VISIBLE);
                ivVerifiedBadge.setVisibility(View.VISIBLE);
            } else {
                ivExpertBadge.setVisibility(View.GONE);
                ivVerifiedBadge.setVisibility(View.GONE);
            }

            // Set follow button state
            updateFollowButton(user.getIsFollowing());

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (userClickListener != null) {
                    userClickListener.onUserClick(user);
                }
            });

            btnFollow.setOnClickListener(v -> {
                if (followClickListener != null) {
                    followClickListener.onFollowClick(user, user.getIsFollowing());
                }
            });
        }

        private void updateFollowButton(boolean isFollowing) {
            if (isFollowing) {
                btnFollow.setText("Following");
                btnFollow.setBackgroundTintList(context.getColorStateList(R.color.text_secondary_dark));
            } else {
                btnFollow.setText("Follow");
                btnFollow.setBackgroundTintList(context.getColorStateList(R.color.primary));
            }
        }
    }
}
