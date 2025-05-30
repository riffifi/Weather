package io.levs.weather;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    private ImageView splashIcon;
    private TextView appNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashIcon = findViewById(R.id.splashIcon);
        appNameTextView = findViewById(R.id.appNameTextView);

        splashIcon.setAlpha(0f);
        appNameTextView.setAlpha(0f);

        animateSplashScreen();
    }

    private void animateSplashScreen() {
        ObjectAnimator iconFadeIn = ObjectAnimator.ofFloat(splashIcon, View.ALPHA, 0f, 1f);
        iconFadeIn.setDuration(800);
        
        ObjectAnimator iconScale = ObjectAnimator.ofFloat(splashIcon, View.SCALE_X, 0.6f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(splashIcon, View.SCALE_Y, 0.6f, 1f);
        iconScale.setDuration(800);
        iconScaleY.setDuration(800);
        
        ObjectAnimator textFadeIn = ObjectAnimator.ofFloat(appNameTextView, View.ALPHA, 0f, 1f);
        textFadeIn.setDuration(600);
        textFadeIn.setStartDelay(300);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(iconFadeIn, iconScale, iconScaleY, textFadeIn);
        animatorSet.start();
        
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1800);
    }
}
