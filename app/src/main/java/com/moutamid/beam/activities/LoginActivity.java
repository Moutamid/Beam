package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.moutamid.beam.databinding.ActivityLoginBinding;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    ActivityLoginBinding binding;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;

    SpeechUtils speechUtils = new SpeechUtils() {
        @Override
        public void onResult(String result) {
            Log.d(TAG, "onResult: " + result);
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            if (result.toLowerCase(Locale.ROOT).contains("go back")) {
                getOnBackPressedDispatcher().onBackPressed();
            } else if (result.toLowerCase(Locale.ROOT).contains("login")) {
                login();
            } else if (result.toLowerCase(Locale.ROOT).contains("select phone")) {
                binding.phone.getEditText().requestFocus();
            } else if (result.toLowerCase(Locale.ROOT).contains("write phone number")){
                binding.phone.getEditText().requestFocus();
            } else {
                if (binding.phone.getEditText().hasFocus()) {
                    binding.phone.getEditText().setText(result.replace(" ", "").replace("-", ""));
                }
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                finish();
            }
        });

        binding.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.mic.listen.setOnClickListener(v -> {
            if (listeningAnimation == null || !listeningAnimation.isRunning()) {
                listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                speechRecognitionManager.startListening();
            } else {
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
                listeningAnimation = null;
                speechRecognitionManager.stopListening();
            }
        });

        binding.create.setOnClickListener(v -> {
            login();
        });

    }

    private void login() {
        if (valid()) {
            UserModel userModel = new UserModel();
            userModel.phoneNumber = binding.ccp.getSelectedCountryCodeWithPlus() + binding.phone.getEditText().getText().toString();
            Stash.put(Constants.STASH_USER, userModel);
            startActivity(new Intent(this, OtpActivity.class));
        }
    }

    private boolean valid() {
        if (binding.phone.getEditText().getText().toString().isEmpty()) {
            binding.phone.getEditText().setError("required*");
            binding.phone.getEditText().requestFocus();
            return false;
        }
        return true;
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

    @Override
    protected void onPause() {
        super.onPause();
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        speechRecognitionManager.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        speechRecognitionManager.destroy();
    }

}