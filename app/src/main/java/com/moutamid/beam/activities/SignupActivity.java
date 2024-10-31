package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.databinding.ActivitySignupBinding;
import com.moutamid.beam.models.CategoryModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {
    ActivitySignupBinding binding;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    private static final String TAG = "SignupActivity";
    private String[] service_categories = new String[]{};

    SpeechUtils speechUtils = new SpeechUtils() {
        @Override
        public void onResult(String result) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            if (result.toLowerCase(Locale.ROOT).contains("go back")) {
                getOnBackPressedDispatcher().onBackPressed();
            } else if (result.toLowerCase(Locale.ROOT).contains("create account")) {
                create();
            } else if (result.toLowerCase(Locale.ROOT).contains("select name")) {
                binding.name.getEditText().requestFocus();
            } else if (result.toLowerCase(Locale.ROOT).contains("select phone")) {
                binding.phone.getEditText().requestFocus();
            } else if (result.toLowerCase(Locale.ROOT).contains("write phone number")) {
                binding.phone.getEditText().requestFocus();
            } else {
                if (binding.name.getEditText().hasFocus()) {
                    binding.name.getEditText().setText(result);
                }
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
            Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

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

        ArrayList<CategoryModel> category = new ArrayList<>();
        Constants.databaseReference().child(Constants.CATEGORIES).get().addOnSuccessListener(snapshot -> {
            Constants.dismissDialog();
            if (snapshot.exists()) {
                category.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CategoryModel topicsModel = dataSnapshot.getValue(CategoryModel.class);
                    category.add(topicsModel);
                }
                category.sort(Comparator.comparing(categoryModel -> categoryModel.name));
                service_categories = category.stream()
                        .map(cat -> cat.name)
                        .toArray(String[]::new);
                ArrayAdapter<String> subject = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, service_categories);
                binding.categoryList.setAdapter(subject);
            }
        });

        binding.create.setOnClickListener(v -> {
            create();
        });
    }

    private void create() {
        if (valid()) {
            UserModel userModel = new UserModel();
            userModel.name = binding.name.getEditText().getText().toString().trim();
            userModel.isAnonymous = false;
            userModel.category = binding.category.getEditText().getText().toString().trim();
            userModel.phoneNumber = binding.ccp.getSelectedCountryCodeWithPlus() + binding.phone.getEditText().getText().toString().trim();
            Stash.put(Constants.STASH_USER, userModel);
            startActivity(new Intent(this, OtpActivity.class));
        }
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
        // Cancel the animation when the activity is paused
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        Speech.init(this, getPackageName());
        speechRecognitionManager.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the animation when the activity is destroyed
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        Speech.init(this, getPackageName());
        speechRecognitionManager.destroy();
    }

}