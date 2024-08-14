package com.moutamid.beam.activities;

import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fxn.stash.Stash;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivitySignupBinding;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;

import java.util.List;

public class SignupActivity extends AppCompatActivity {
    ActivitySignupBinding binding;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    private static final String TAG = "SignupActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(SignupActivity.this, WelcomeActivity.class));
                finish();
            }
        });

        binding.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);

        speechRecognitionManager = new SpeechRecognitionManager(this, new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                // Handle the speech recognition results
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    Log.d(TAG, "onResults: " + spokenText);
                }
            }

            @Override
            public void onError(int error) {
                // Handle the error
            }

            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognitionManager.startListening();

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
            if (valid()) {
                UserModel userModel = new UserModel();
                userModel.name = binding.name.getEditText().getText().toString();
                userModel.phoneNumber = binding.ccp.getSelectedCountryCodeWithPlus() + binding.phone.getEditText().getText().toString();
                Stash.put(Constants.STASH_USER, userModel);
                startActivity(new Intent(this, OtpActivity.class));
            }
        });
    }

    private boolean valid() {
        if (binding.phone.getEditText().getText().toString().isEmpty()){
            binding.phone.getEditText().setError("required*");
            binding.phone.getEditText().requestFocus();
            return false;
        }
        if (binding.name.getEditText().getText().toString().isEmpty()){
            binding.name.getEditText().setError("required*");
            binding.name.getEditText().requestFocus();
            return false;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Cancel the animation when the activity is paused
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        speechRecognitionManager.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the animation when the activity is destroyed
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        speechRecognitionManager.destroy();
    }

}