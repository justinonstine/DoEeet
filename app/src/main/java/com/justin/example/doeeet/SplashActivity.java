package com.justin.example.doeeet;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

/**
 * Created by Erin on 3/18/15.
 */
public class SplashActivity extends Activity {

    private static int SPLASH_TIME_OUT = 6000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        VideoView videoView = (VideoView) findViewById(R.id.splash_videoview);

//        new Handler().postDelayed(new Runnable () {
//            @Override
//            public void run() {
//                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//
//        }, SPLASH_TIME_OUT);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.main_fade_in, R.anim.splash_fade_out);
            }
        });
        Uri videoUri = Uri.parse("android.resource://com.justin.example.doeeet/" + R.raw.doeeet);
        videoView.setVideoURI(videoUri);
        videoView.start();
    }
}
