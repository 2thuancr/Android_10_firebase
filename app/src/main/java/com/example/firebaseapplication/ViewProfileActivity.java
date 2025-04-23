package com.example.firebaseapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu chọn ảnh
    private CircleImageView imgCurrentUserAvatar;  // Đối tượng CircleImageView hiển thị ảnh đại diện

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_profile);

        // Khởi tạo các View (phần tử giao diện)
        imgCurrentUserAvatar = findViewById(R.id.imgCurrentUserAvatar);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnChangeAvatar = findViewById(R.id.btnChangeAvatar);

        // Đặt sự kiện cho nút quay lại
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
            }
        }
    }
}
