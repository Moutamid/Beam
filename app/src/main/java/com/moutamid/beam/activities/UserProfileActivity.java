package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityUserProfileBinding;
import com.moutamid.beam.fragments.ChatFragment;
import com.moutamid.beam.fragments.MapFragment;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

import net.gotev.speech.Speech;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    ActivityUserProfileBinding binding;
    UserModel userModel;
    String userID;
    UserModel stash;


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
            } else if (result.toLowerCase(Locale.ROOT).contains("open map")) {
                showMap();
            }  else if (result.toLowerCase(Locale.ROOT).contains("open chat")) {
                chat();
            } else if (result.toLowerCase(Locale.ROOT).contains("rate user")) {
                attachRating();
            }  else if (result.toLowerCase(Locale.ROOT).contains("call user")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userModel.phoneNumber));
                startActivity(intent);
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.title.setText("user");
        binding.toolbar.stop.setVisibility(View.VISIBLE);
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        userID = getIntent().getStringExtra("USER_ID");

        Constants.databaseReference().child(Constants.USER).child(userID).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()){
                userModel = dataSnapshot.getValue(UserModel.class);

                binding.name.setText(userModel.name);
                binding.phone.setText(userModel.phoneNumber);
                Glide.with(this).load(userModel.image).placeholder(R.drawable.profile_icon).into(binding.image);

                binding.phone.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userModel.phoneNumber));
                    startActivity(intent);
                });

                if (userModel.status) {
                    binding.status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.green)));
                } else {
                    binding.status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.stroke)));
                }

                if (userModel.rating != null) {
                    float rating = 0;
                    for (double commentModel : userModel.rating) rating += commentModel;
                    float total = rating / userModel.rating.size();
                    String rate = String.format(Locale.getDefault(), "%.2f", total) + " (" + userModel.rating.size() + ")";
                    if (userModel.rating.size() > 1) binding.rating.setText(rate);
                    else binding.rating.setText(userModel.rating.get(0) + " (1)");
                } else {
                    binding.rating.setText("0.0 (0)");
                }

                stash = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);

                double distance = Constants.calculateDistance(stash.location.lat, stash.location.log, userModel.location.lat, userModel.location.log);
                binding.distance.setText(Constants.formatDistance(distance));

                LatLng currentLatLng = new LatLng(userModel.location.lat, userModel.location.log);
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MapFragment(currentLatLng)).commit();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });

        binding.map.setOnClickListener(v -> {
            showMap();
        });

        binding.chat.setOnClickListener(v -> {
            chat();
        });

        binding.attachRating.setOnClickListener(v -> {
            attachRating();
        });


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

    }

    private void chat() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChatFragment(userModel)).commit();
    }

    private void attachRating() {
        if (userID.equals(Constants.auth().getCurrentUser().getUid())) {
            Toast.makeText(this, "You cant rate yourself", Toast.LENGTH_SHORT).show();
        } else showRatingDialog();
    }

    private void showMap() {
        LatLng currentLatLng = new LatLng(userModel.location.lat, userModel.location.log);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MapFragment(currentLatLng)).commit();
    }

    private void showRatingDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_review);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        MaterialButton submit = dialog.findViewById(R.id.confirm);
        SimpleRatingBar rating = dialog.findViewById(R.id.rating);

        submit.setOnClickListener(v -> {
            Map<String, Object> map = new HashMap<>();
            ArrayList<Float> list = userModel.rating == null ? new ArrayList<>() : userModel.rating;
            list.add(rating.getRating());
            userModel.rating = new ArrayList<>(list);
            map.put("rating", list);
            Constants.databaseReference().child(Constants.USER).child(userModel.id).updateChildren(map).addOnSuccessListener(unused -> {
                dialog.dismiss();
                updateRating();
                Toast.makeText(this, "Thanks for your feedback", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            });
        });

    }

    private void updateRating() {
        if (userModel.rating != null) {
            float rating = 0;
            for (double commentModel : userModel.rating) rating += commentModel;
            float total = rating / userModel.rating.size();
            String rate = String.format(Locale.getDefault(), "%.2f", total) + " (" + userModel.rating.size() + ")";
            if (userModel.rating.size() > 1) binding.rating.setText(rate);
            else binding.rating.setText(userModel.rating.get(0) + " (1)");
        } else {
            binding.rating.setText("0.0 (0)");
        }
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
}