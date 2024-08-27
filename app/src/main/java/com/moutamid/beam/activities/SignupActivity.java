package com.moutamid.beam.activities;

import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.moutamid.beam.utilis.Stash;
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
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
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

        String[] service_categories = getResources().getStringArray(R.array.service_categories);
        ArrayAdapter<String> subject = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, service_categories);
        binding.categoryList.setAdapter(subject);

        binding.create.setOnClickListener(v -> {
            if (valid()) {
                UserModel userModel = new UserModel();
                userModel.name = binding.name.getEditText().getText().toString().trim();
                userModel.category = binding.category.getEditText().getText().toString().trim();
                userModel.phoneNumber = binding.ccp.getSelectedCountryCodeWithPlus() + binding.phone.getEditText().getText().toString().trim();
                Stash.put(Constants.STASH_USER, userModel);
                startActivity(new Intent(this, OtpActivity.class));
            }
        });
    }

    private boolean valid() {
        if (binding.name.getEditText().getText().toString().isEmpty()) {
            binding.name.getEditText().setError("required*");
            binding.name.getEditText().requestFocus();
            return false;
        }
        if (binding.phone.getEditText().getText().toString().isEmpty()) {
            binding.phone.getEditText().setError("required*");
            binding.phone.getEditText().requestFocus();
            return false;
        }
        if (binding.category.getEditText().getText().toString().isEmpty()) {
            binding.category.getEditText().setError("required*");
            binding.category.getEditText().requestFocus();
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