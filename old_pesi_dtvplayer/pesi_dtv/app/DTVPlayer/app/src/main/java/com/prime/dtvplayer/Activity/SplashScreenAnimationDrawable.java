package com.prime.dtvplayer.Activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.prime.dtvplayer.R;

public class SplashScreenAnimationDrawable extends AppCompatActivity {
    private static boolean firstTime=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(firstTime == true) {
            firstTime = false;
            setContentView(R.layout.activity_splash_animation_drawable);

            ImageView img = (ImageView) findViewById(R.id.imageViewSplash);
            img.setBackgroundResource(R.drawable.spin_animation);
            // Get the background, which has been compiled to an AnimationDrawable object.
            AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
            frameAnimation.setOneShot(true);
            // Start the animation
            frameAnimation.setCallback(new AnimationDrawableCallback(frameAnimation, img) {
                @Override
                public void onAnimationComplete() {
                    // TODO Do something.
                    jump();
                }
            });
            frameAnimation.start();
        }else
            jump();
    }

    private void jump(){
        // Jump to your Next Activity or MainActivity
        SplashScreenAnimationDrawable.this.finish();
        Intent intent = new Intent(SplashScreenAnimationDrawable.this, ViewActivity.class);
        startActivity(intent);


        //eric lin: close the activity switch animation
        this.overridePendingTransition(0, 0);
    }
}
