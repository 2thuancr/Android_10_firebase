package com.example.firebaseapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private VideosFireBaseAdapter videosAdapter;
    private CircleImageView imgCurrentUserAvatar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference videosRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        imgCurrentUserAvatar = findViewById(R.id.imgCurrentUserAvatar);

        database = FirebaseDatabase.getInstance();
        videosRef = database.getReference("videos");

        ArrayList<Video1Model> videos = initVideos();
        for (Video1Model video : videos) {
            uploadVideoData(video);
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Người dùng chưa đăng nhập
            finish();
            return;
        }
        String uid = currentUser.getUid();
        if (currentUser.getPhotoUrl() != null) {
            // Dùng Picasso để load avatar lên View (giả sử imgCurrentUserAvatar là ImageView hoặc CircleImageView)
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.default_avatar) // avatar mặc định nếu chưa có
                    .error(R.drawable.default_avatar)       // avatar lỗi
                    .into(imgCurrentUserAvatar); // ép kiểu vì imgCurrentUserAvatar đang là View
        }

        imgCurrentUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewProfileActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager2 = findViewById(R.id.vpager); // Đặt ngoài listener
        getVideos(); // Cũng gọi ngoài listener
    }

    private void getVideos() {
        /*#set databases*/
        DatabaseReference mDataBase = FirebaseDatabase.getInstance().getReference("videos");
        FirebaseRecyclerOptions<Video1Model> options = new FirebaseRecyclerOptions.Builder<Video1Model>()
                .setQuery(mDataBase, Video1Model.class).build();
        /*#set adapter*/
        videosAdapter = new VideosFireBaseAdapter(options);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setAdapter(videosAdapter);
    }


    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                // Load ảnh avatar từ Firestore (dùng Picasso)
                                Picasso.get()
                                        .load(avatarUrl)
                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                                        .placeholder(R.drawable.default_avatar)
                                        .error(R.drawable.default_avatar)
                                        .into(imgCurrentUserAvatar); // imgCurrentUserAvatar là ImageView của bạn
                            }
                        }
                    });
        }
    }

    private ArrayList<Video1Model> initVideos() {
        ArrayList<Video1Model> videos = new ArrayList<>();

        videos.add(
                new Video1Model(
                        1,
                        "Video Description",
                        "https://videos.pexels.com/video-files/6752408/6752408-uhd_1440_2732_25fps.mp4",
                        "cottonbro studio",
                        "8FL1egvnNdfmCt0Zm8QD7YVS4fw1"
        ));

        videos.add(
                new Video1Model(
                        2,
                        "Video Description",
                        "https://videos.pexels.com/video-files/8371249/8371249-uhd_1440_2732_25fps.mp4",
                        "Ron Lach",
                        "vq5cnKxV72gCSj6FPArDRnqK0Fp1"
                ));

        videos.add(
                new Video1Model(
                        3,
                        "Video Description",
                        "https://videos.pexels.com/video-files/5082036/5082036-uhd_1440_2732_25fps.mp4",
                        "Two people are holding up their phones to take a picture",
                        "vq5cnKxV72gCSj6FPArDRnqK0Fp1"
                ));

        videos.add(
                new Video1Model(
                        4,
                        "Video Description",
                        "https://videos.pexels.com/video-files/7744213/7744213-uhd_1440_2732_25fps.mp4",
                        "MART PRODUCTION",
                        "vq5cnKxV72gCSj6FPArDRnqK0Fp1"
                ));

        return videos;
    }

    private void uploadVideoData(Video1Model video) {
        // Kiểm tra sự tồn tại của video trước khi thêm
        Query query = videosRef.orderByChild("url").equalTo(video.getUrl());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("Firebase", "Video already exists!");
                } else {
                    // Video chưa tồn tại, thực hiện thêm video mới vào database
                    videosRef.push().setValue(video).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "Video added successfully!");
                        } else {
                            Log.e("Firebase", "Failed to add video", task.getException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Failed to check video existence", databaseError.toException());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videosAdapter != null) {
            videosAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        videosAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videosAdapter.notifyDataSetChanged();
        loadUserInfo(); // Gọi lại hàm tải thông tin người dùng mỗi khi quay lại màn hình
    }
}