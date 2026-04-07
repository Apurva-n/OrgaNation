package com.organation.organation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    // UI Components
    private ImageView appLogo, glowRing, pulseEffect, bgHeart1, bgHeart2, bgHeart3, bgHeart4;
    private TextView appName, appTagline, quoteText, quoteAuthor, loadingText;
    private ProgressBar progressBar;
    
    // Animation Variables
    private Handler handler;
    private Random random;
    private int currentQuoteIndex = 0;
    
    // Splash Duration
    private static final int SPLASH_DURATION = 4000; // 4 seconds
    private static final int QUOTE_CHANGE_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        
        initializeViews();
        setupAnimations();
        startQuoteRotation();
        navigateToMainScreen();
    }
    
    private void initializeViews() {
        // Logo and Effects
        appLogo = findViewById(R.id.appLogo);
        glowRing = findViewById(R.id.glowRing);
        pulseEffect = findViewById(R.id.pulseEffect);
        
        // Background Hearts
        bgHeart1 = findViewById(R.id.bgHeart1);
        bgHeart2 = findViewById(R.id.bgHeart2);
        bgHeart3 = findViewById(R.id.bgHeart3);
        bgHeart4 = findViewById(R.id.bgHeart4);
        
        // Text Elements
        appName = findViewById(R.id.appName);
        appTagline = findViewById(R.id.appTagline);
        quoteText = findViewById(R.id.quoteText);
        quoteAuthor = findViewById(R.id.quoteAuthor);
        loadingText = findViewById(R.id.loadingText);
        
        // Progress
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize Handler and Random
        handler = new Handler();
        random = new Random();
    }
    
    private void setupAnimations() {
        // Logo Entrance Animation
        animateLogoEntrance();
        
        // Background Hearts Floating Animation
        animateBackgroundHearts();
        
        // Glow Ring Rotation
        animateGlowRing();
        
        // Pulse Effect
        startPulseAnimation();
        
        // Text Animations
        animateTextElements();
    }
    
    private void animateLogoEntrance() {
        // Scale up animation for logo
        ScaleAnimation scaleAnimation = new ScaleAnimation(
            0.0f, 1.0f, 0.0f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(1000);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        appLogo.startAnimation(scaleAnimation);
        
        // Glow ring fade in
        glowRing.setAlpha(0f);
        glowRing.animate()
            .alpha(0.3f)
            .setDuration(1500)
            .setStartDelay(500)
            .start();
    }
    
    private void animateBackgroundHearts() {
        // Animate background hearts with different delays and movements
        animateHeartFloating(bgHeart1, 3000, 0);
        animateHeartFloating(bgHeart2, 3500, 500);
        animateHeartFloating(bgHeart3, 3200, 1000);
        animateHeartFloating(bgHeart4, 2800, 1500);
    }
    
    private void animateHeartFloating(ImageView heart, long duration, long delay) {
        heart.setAlpha(0f);
        
        // Fade in
        heart.animate()
            .alpha(heart.getAlpha())
            .setDuration(1000)
            .setStartDelay(delay)
            .start();
        
        // Floating animation
        handler.postDelayed(() -> {
            heart.animate()
                .translationY(-50f)
                .setDuration(duration / 2)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    heart.animate()
                        .translationY(0f)
                        .setDuration(duration / 2)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(() -> animateHeartFloating(heart, duration, 0))
                        .start();
                })
                .start();
        }, delay);
    }
    
    private void animateGlowRing() {
        RotateAnimation rotateAnimation = new RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        glowRing.startAnimation(rotateAnimation);
    }
    
    private void startPulseAnimation() {
        handler.postDelayed(() -> {
            pulseEffect.setAlpha(0f);
            pulseEffect.animate()
                .alpha(0.5f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(1000)
                .withEndAction(() -> {
                    pulseEffect.animate()
                        .alpha(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1000)
                        .withEndAction(this::startPulseAnimation)
                        .start();
                })
                .start();
        }, 2000);
    }
    
    private void animateTextElements() {
        // App name slide up animation
        appName.setTranslationY(100f);
        appName.setAlpha(0f);
        appName.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(1200)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
        
        // Tagline slide up animation
        appTagline.setTranslationY(100f);
        appTagline.setAlpha(0f);
        appTagline.animate()
            .translationY(0f)
            .alpha(0.9f)
            .setDuration(800)
            .setStartDelay(1400)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
        
        // Quote container fade in
        findViewById(R.id.quoteContainer).setAlpha(0f);
        findViewById(R.id.quoteContainer).animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1800)
            .start();
        
        // Quote icon rotation
        ImageView quoteIcon = findViewById(R.id.quoteIcon);
        RotateAnimation quoteRotation = new RotateAnimation(
            0f, 15f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        quoteRotation.setDuration(2000);
        quoteRotation.setRepeatCount(Animation.INFINITE);
        quoteRotation.setRepeatMode(Animation.REVERSE);
        quoteRotation.setInterpolator(new AccelerateDecelerateInterpolator());
        quoteIcon.startAnimation(quoteRotation);
    }
    
    private void startQuoteRotation() {
        // Set initial quote
        updateQuote();
        
        // Rotate quotes every 2 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateQuote();
                handler.postDelayed(this, QUOTE_CHANGE_INTERVAL);
            }
        }, QUOTE_CHANGE_INTERVAL);
    }
    
    private void updateQuote() {
        String[] quotes = getResources().getStringArray(R.array.inspirational_quotes);
        String[] authors = getResources().getStringArray(R.array.quote_authors);
        
        // Get random quote
        currentQuoteIndex = random.nextInt(quotes.length);
        String quote = quotes[currentQuoteIndex];
        String author = authors[currentQuoteIndex];
        
        // Fade out current quote
        quoteText.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> {
                // Update text
                quoteText.setText(quote);
                quoteAuthor.setText(author);
                
                // Fade in new quote
                quoteText.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
                
                quoteAuthor.animate()
                    .alpha(0.8f)
                    .setDuration(300)
                    .start();
            })
            .start();
    }
    
    private void navigateToMainScreen() {
        // Show loading progress after 3 seconds
        handler.postDelayed(() -> {
            showLoadingProgress();
        }, 3000);
        
        // Navigate to main screen after splash duration
        handler.postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DURATION);
    }
    
    private void showLoadingProgress() {
        // Hide quote container
        findViewById(R.id.quoteContainer).animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction(() -> {
                findViewById(R.id.quoteContainer).setVisibility(android.view.View.GONE);
            })
            .start();
        
        // Show loading container
        findViewById(R.id.loadingContainer).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.loadingContainer).setAlpha(0f);
        findViewById(R.id.loadingContainer).animate()
            .alpha(1f)
            .setDuration(500)
            .start();
        
        // Start progress bar animation
        progressBar.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
    }
    
    private void navigateToNextScreen() {
        // Create intent for universal signup (or main activity)
        Intent intent = new Intent(this, Donor_login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Add transition animation
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        // Finish splash activity
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up handler to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
    
   /* @Override
    public void onBackPressed() {
        // Prevent back button press during splash
        // User must wait for splash to complete
    }  */
}
