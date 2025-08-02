package com.evenbus.myapplication.leak.videoleak;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_memory.R;


// VideoPlayerActivity.java
public class VideoPlayerActivity extends AppCompatActivity {
    private CoverVideoPlayerView videoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videoPlayer = findViewById(R.id.video_player);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 开发者忘记调用release方法
        // videoPlayer.release();
    }
}