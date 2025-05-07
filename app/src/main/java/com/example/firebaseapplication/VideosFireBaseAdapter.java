package com.example.firebaseapplication;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class VideosFireBaseAdapter extends FirebaseRecyclerAdapter<Video1Model, VideosFireBaseAdapter.MyHolder> {
    private boolean isFav = false;

    public VideosFireBaseAdapter(@NonNull FirebaseRecyclerOptions<Video1Model> options) {
        super(options);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_video_row, parent, false);
        return new MyHolder(view);
    }
    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull Video1Model model) {
        holder.textVideoTitle.setText(model.getTitle());
        holder.textVideoDescription.setText(model.getDesc());
        holder.videoView.setVideoURI(Uri.parse(model.getUrl()));

        // ✅ Load avatar từ Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(model.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatarUrl = documentSnapshot.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(holder.imgUploaderAvatar.getContext())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.default_avatar)
                                    .error(R.drawable.default_avatar)
                                    .into(holder.imgUploaderAvatar);
                        } else {
                            holder.imgUploaderAvatar.setImageResource(R.drawable.default_avatar);
                        }
                    } else {
                        holder.imgUploaderAvatar.setImageResource(R.drawable.default_avatar);
                    }
                    // Load email hoặc tên
                    String email = documentSnapshot.getString("email"); // hoặc "name" tùy bạn lưu gì
                    if (email != null && !email.isEmpty()) {
                        holder.txtUploaderEmail.setText(email);
                    } else {
                        holder.txtUploaderEmail.setText("Người dùng ẩn danh");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.imgUploaderAvatar.setImageResource(R.drawable.default_avatar);
                    Log.e("AVATAR_LOAD", "Lỗi khi tải avatar cho userId: " + model.getUserId(), e);
                });

        holder.videoView.setOnPreparedListener(mp -> {
            holder.videoProgressBar.setVisibility(View.GONE);
            mp.start();

            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
            float scale = videoRatio / screenRatio;

            if (scale >= 1f) {
                holder.videoView.setScaleX(scale);
            } else {
                holder.videoView.setScaleY(1f / scale);
            }
        });

        holder.videoView.setOnCompletionListener(MediaPlayer::start);

        holder.favorites.setOnClickListener(v -> {
            if (!isFav) {
                // holder.favorites.setImageResource(R.drawable.ic_fill_favorite); // Uncomment nếu có icon
                isFav = true;
            } else {
                holder.favorites.setImageResource(R.drawable.ic_favorite);
                isFav = false;
            }
        });
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private VideoView videoView;
        private ProgressBar videoProgressBar;
        private TextView textVideoTitle;
        private TextView textVideoDescription;
        private TextView txtUploaderEmail;
        private ImageView imPerson, favorites, imShare, imMore, imgUploaderAvatar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imgUploaderAvatar = itemView.findViewById(R.id.imgUploaderAvatar);
            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            imPerson = itemView.findViewById(R.id.imgUploaderAvatar);
            favorites = itemView.findViewById(R.id.favorites);
            imShare = itemView.findViewById(R.id.imShare);
            imMore = itemView.findViewById(R.id.imMore);
            txtUploaderEmail = itemView.findViewById(R.id.txtUploaderEmail);

        }
    }
}