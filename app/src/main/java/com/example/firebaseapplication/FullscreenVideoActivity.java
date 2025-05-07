package com.example.firebaseapplication;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.example.firebaseapplication.databinding.ActivityFullscreenVideoBinding;

public class FullscreenVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        videoView = findViewById(R.id.fullscreenVideoView);
        progressBar = findViewById(R.id.fullscreenVideoProgressBar);

        String videoUrl = getIntent().getStringExtra("videoUrl");

        if (videoUrl != null) {
            Uri videoUri = Uri.parse(videoUrl);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                mp.setLooping(true);
                videoView.start();
            });
            videoView.setOnCompletionListener(MediaPlayer::start);
        }
    }

    @Override
    public void onBackPressed() {
        videoView.stopPlayback();
        super.onBackPressed();
    }
}
