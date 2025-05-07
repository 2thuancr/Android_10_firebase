package com.example.firebaseapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu chọn ảnh
    private CircleImageView imgCurrentUserAvatar;  // Đối tượng CircleImageView hiển thị ảnh đại diện
    ImageButton btnBack;
    Button btnChangeAvatar;
    TextView txtProfileEmail;

    TextView txtVideoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Người dùng chưa đăng nhập
            finish();
            return;
        }
        storageReference = FirebaseStorage.getInstance().getReference();

        // Khởi tạo các View (phần tử giao diện)
        imgCurrentUserAvatar = findViewById(R.id.imgCurrentUserAvatar);
        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtVideoCount = findViewById(R.id.txtVideoCount);

        // Đặt sự kiện cho nút quay lại
        btnBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        ViewCompat.setOnApplyWindowInsetsListener(btnBack, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Đặt sự kiện cho nút thay đổi ảnh đại diện
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());

        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            // Dùng Picasso để load avatar lên View (giả sử imgCurrentUserAvatar là ImageView hoặc CircleImageView)
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.default_avatar) // avatar mặc định nếu chưa có
                    .error(R.drawable.default_avatar)       // avatar lỗi
                    .into((ImageView) imgCurrentUserAvatar); // ép kiểu vì imgCurrentUserAvatar đang là View

            txtProfileEmail.setText(currentUser.getEmail());

            DatabaseReference videosRef = FirebaseDatabase.getInstance().getReference("videos");

            videosRef.orderByChild("userId").equalTo(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long videoCount = snapshot.getChildrenCount();
                            txtVideoCount.setText("Số video đã đăng: " + videoCount);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            txtVideoCount.setText("Không thể tải dữ liệu");
                            Log.e("ProfileDetail", "Lỗi khi lấy video: " + error.getMessage());
                        }
                    });
        }


    }

    // Mở trình chọn ảnh để người dùng chọn ảnh mới
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // Chỉ cho phép chọn ảnh
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();  // Lấy URI của ảnh đã chọn

            // Hiển thị ảnh chọn trước
            Picasso.get().load(imageUri).into(imgCurrentUserAvatar);

            // Tạo tên file duy nhất
            String fileName = "avatars/" + currentUser.getUid() + ".jpg";
            StorageReference avatarRef = storageReference.child(fileName);

            try {
                InputStream stream = getContentResolver().openInputStream(imageUri);
                UploadTask uploadTask = avatarRef.putStream(stream);

                uploadTask
                        .addOnSuccessListener(taskSnapshot -> {
                            // Lấy URL của ảnh sau khi tải lên
                            avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String photoUrl = uri.toString();

                                // ✅ Cập nhật vào Firestore
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                DocumentReference userDocRef = firestore.collection("users").document(currentUser.getUid());

                                userDocRef.update("avatarUrl", photoUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Lưu Firestore thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e("FIRESTORE", "Error updating avatarUrl", e);
                                        });

                                // (Không bắt buộc) Cập nhật profile trong Firebase Auth nếu bạn dùng
                                updateUserProfile(photoUrl);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Upload thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("UPLOAD", "Upload error", e);
                        });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void updateUserProfile(String photoUrl) {
        if (currentUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(photoUrl))  // Cập nhật ảnh đại diện mới
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đã cập nhật avatar!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi khi cập nhật profile.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
