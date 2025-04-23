package com.example.firebaseapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu chọn ảnh
    private CircleImageView imgCurrentUserAvatar;  // Đối tượng CircleImageView hiển thị ảnh đại diện
    ImageButton btnBack;
    Button btnChangeAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_profile);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Khởi tạo các View (phần tử giao diện)
        imgCurrentUserAvatar = findViewById(R.id.imgCurrentUserAvatar);
        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);

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
    }

    // Mở trình chọn ảnh để người dùng chọn ảnh mới
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // Chỉ cho phép chọn ảnh
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Xử lý kết quả khi người dùng chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();  // Lấy URI của ảnh đã chọn
            if (imageUri != null) {
                // Sử dụng Picasso để tải ảnh và set vào CircleImageView
                Picasso.get()
                        .load(imageUri)  // Tải ảnh từ URI
                        .into(imgCurrentUserAvatar); // Đặt ảnh vào CircleImageView

                // Tạo tên file duy nhất
                String fileName = "avatars/" + UUID.randomUUID().toString() + ".jpg";
                StorageReference avatarRef = storageReference.child(fileName);

                try {
                    InputStream stream = getContentResolver().openInputStream(imageUri);
                    UploadTask uploadTask = avatarRef.putStream(stream);

                    uploadTask
                            .addOnSuccessListener(taskSnapshot -> {
                                // Lấy URL của ảnh sau khi tải lên
                                avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String photoUrl = uri.toString();
                                    // Cập nhật profile người dùng với URL ảnh vừa tải lên
                                    updateUserProfile(photoUrl);

                                    Toast.makeText(this, "Đã upload ảnh đại diện!", Toast.LENGTH_SHORT).show();
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
    }

    private void updateUserProfile(String photoUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(photoUrl))  // Cập nhật ảnh đại diện mới
                    .build();

            user.updateProfile(profileUpdates)
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
