package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityWelcomeBinding;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;

import net.gotev.speech.Speech;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    ActivityWelcomeBinding binding;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS}, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        }

        binding.mic.listen.setOnClickListener(v -> {
            if (listeningAnimation == null || !listeningAnimation.isRunning()) {
                listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                Speech.init(this, getPackageName());
                speechRecognitionManager.startListening();
                Log.d(TAG, "onCreate: START");
            } else {
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
                listeningAnimation = null;
                speechRecognitionManager.stopListening();
                Log.d(TAG, "onCreate: END");
            }
        });

        binding.login.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.signup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    Speech.init(this, getPackageName());
                    speechRecognitionManager = new SpeechRecognitionManager(this, speechUtils);
                    listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                    speechRecognitionManager.startListening();
                });
            }, 1000);
        }
    }

    SpeechUtils speechUtils = new SpeechUtils() {
        @Override
        public void onResult(String result) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Log.d(TAG, "onResult: " + result);
            if (result.toLowerCase(Locale.ROOT).contains("login")) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                finish();
            } else if (result.toLowerCase(Locale.ROOT).contains("signup") || result.toLowerCase(Locale.ROOT).contains("sign up")) {
                startActivity(new Intent(WelcomeActivity.this, SignupActivity.class));
                finish();
            } else if (result.toLowerCase(Locale.ROOT).contains("google")) {
                signInGoogle();
            } else if (result.toLowerCase(Locale.ROOT).contains("facebook")) {
                signInFacebook();
            } else if (result.toLowerCase(Locale.ROOT).contains("linkedIn")) {
                signInLinkedIn();
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(WelcomeActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    private void signInLinkedIn() {

    }

    private void signInGoogle() {

    }

    private void signInFacebook() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cancel the animation when the activity is paused
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        if (speechRecognitionManager != null) {
            speechRecognitionManager.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the animation when the activity is destroyed
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        if (speechRecognitionManager != null) {
            speechRecognitionManager.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Speech.init(this, getPackageName());
                speechRecognitionManager = new SpeechRecognitionManager(this, speechUtils);
                listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                speechRecognitionManager.startListening();
            } else {
                Toast.makeText(this, "Permission for recording audio was denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}