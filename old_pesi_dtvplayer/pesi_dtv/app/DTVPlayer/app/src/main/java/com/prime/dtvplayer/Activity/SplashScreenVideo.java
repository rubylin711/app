package com.prime.dtvplayer.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.prime.dtvplayer.R;

public class SplashScreenVideo extends AppCompatActivity {

    private boolean ispaused = false;
    private static boolean firstTime=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Darken the status bar (optional - Create your own Utils Class)
        //MyUtils.darkenStatusBar(this, R.color.splash_grey);
        if(firstTime == true) {
            firstTime = false;
            setContentView(R.layout.activity_splash_video);

            MutedVideoView vView = (MutedVideoView) findViewById(R.id.video_view);
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.splash);

            if (vView != null) {
                vView.setVideoURI(video);
                vView.setZOrderOnTop(true);
                vView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        jump();
                    }
                });


                vView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        jump();
                        return false;
                    }
                });

                vView.start();

            } else {

                jump();
            }
        }else
            jump();
    }


    private void jump() {

        // Jump to your Next Activity or MainActivity
        Intent intent = new Intent(SplashScreenVideo.this, ViewActivity.class);
        startActivity(intent);

        SplashScreenVideo.this.finish();

        //eric lin: close the activity switch animation
        this.overridePendingTransition(0, 0);
    }


    @Override
    protected void onPause() {
        super.onPause();
        ispaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ispaused) {
            jump();
        }

    }

}
