package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityUserProfileBinding;
import com.moutamid.beam.fragments.ChatFragment;
import com.moutamid.beam.fragments.MapFragment;
import com.moutamid.beam.models.OrderModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    ActivityUserProfileBinding binding;
    UserModel userModel;
    String userID, REQUEST_ID, REQUESTER_ID;
    UserModel stash;
    boolean isActive = false;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    private boolean isChatOpen = false;
    ChatFragment chatFragment;
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
            } else if (result.toLowerCase(Locale.ROOT).contains("open chat")) {
                chat();
            } else if (result.toLowerCase(Locale.ROOT).contains("activate order")) {
                activeOrder();
            } else if (result.toLowerCase(Locale.ROOT).contains("close order")) {
                closeOrder();
            } else if (result.toLowerCase(Locale.ROOT).contains("call user")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userModel.phoneNumber));
                startActivity(intent);
            } else if (isChatOpen) {
                if (result.toLowerCase(Locale.ROOT).contains("send")) {
                    if (!chatFragment.message.getText().toString().isEmpty()) {
                        chatFragment.sendMessage();
                    }
                } else {
                    chatFragment.message.append(result + " ");
                    chatFragment.message.setSelection(chatFragment.message.getText().length());
                }
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
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        userID = getIntent().getStringExtra("USER_ID");
        REQUEST_ID = getIntent().getStringExtra("REQUEST_ID");
        REQUESTER_ID = getIntent().getStringExtra("REQUESTER_ID");

        boolean PREVIEW = getIntent().getBooleanExtra("PREVIEW", false);

        Log.d("SplashActivity", "PREVIEW: " + PREVIEW);

        if (PREVIEW) {
            binding.map.setVisibility(View.GONE);
            binding.activeClose.setVisibility(View.GONE);
        } else {
            checkOrder();
        }

        Log.d(TAG, "userID: " + userID);
        Log.d(TAG, "REQUEST_ID: " + REQUEST_ID);
        Log.d(TAG, "REQUESTER_ID: " + REQUESTER_ID);

        Constants.databaseReference().child(Constants.USER).child(userID).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
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

                if (PREVIEW) {
                    chat();
                } else {
                    if (userID.equals(Constants.ADMIN_ID)) {
                        chat();
                    } else {
                        showMap();
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });

        if (!PREVIEW) {
            if (userID.equals(Constants.ADMIN_ID)) {
                binding.map.setVisibility(View.GONE);
            } else binding.map.setVisibility(View.VISIBLE);
        }

        binding.map.setOnClickListener(v -> {
            showMap();
        });

        binding.chat.setOnClickListener(v -> {
            chat();
        });

        binding.activeClose.setOnClickListener(v -> {
            if (isActive) closeOrder();
            else activeOrder();
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

        binding.main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                binding.main.getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.main.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
                    listeningAnimation = null;
                    speechRecognitionManager.stopListening();
                    binding.micLayout.setVisibility(View.GONE);
                    Log.d(TAG, "onGlobalLayout: Keyboard OPEN");
                } else {
                    Log.d(TAG, "onGlobalLayout: Keyboard CLOSE");
                    binding.micLayout.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void closeOrder() {
        if (order != null) {
            Constants.databaseReference().child(Constants.ORDER).child(Constants.auth().getCurrentUser().getUid()).child(order.getId()).removeValue()
                    .addOnSuccessListener(unused -> {
                        isActive = false;
                        binding.checkIcon.setImageResource(R.drawable.circle_check_solid);
                        binding.activeText.setText("Activate Order");
                        attachRating();
                    });
        } else {
            attachRating();
        }
    }

    private void checkOrder() {
        Constants.databaseReference().child(Constants.ORDER).child(Constants.auth().getCurrentUser().getUid())
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            order = snapshot.getValue(OrderModel.class);
                            if (order.userID.equals(userID) && order.requesterID.equals(REQUESTER_ID)) {
                                isActive = true;
                                binding.checkIcon.setImageResource(R.drawable.circle_xmark_solid);
                                binding.activeText.setText("Close Order");
                                break;
                            } else {
                                order = null;
                            }
                        }
                    } else {
                        isActive = false;
                    }
                });
    }

    OrderModel order;

    private void activeOrder() {
        order = new OrderModel();
        order.setId(UUID.randomUUID().toString());
        order.setUserID(userID);
        order.setRequestID(REQUEST_ID);
        order.setRequesterID(REQUESTER_ID);
        Constants.databaseReference().child(Constants.ORDER).child(Constants.auth().getCurrentUser().getUid())
                .child(order.getId()).setValue(order)
                .addOnSuccessListener(unused -> {
                    activeOrderUser();
                });
    }

    private void activeOrderUser() {
        OrderModel order = new OrderModel();
        order.setId(UUID.randomUUID().toString());
        order.setUserID(Constants.auth().getCurrentUser().getUid());
        order.setRequestID(REQUEST_ID);
        order.setRequesterID(REQUESTER_ID);
        Constants.databaseReference().child(Constants.ORDER).child(userID)
                .child(order.getId()).setValue(order)
                .addOnSuccessListener(unused -> {
                    isActive = true;
                    binding.checkIcon.setImageResource(R.drawable.circle_xmark_solid);
                    binding.activeText.setText("Close Order");
                });
    }

    private void chat() {
        isChatOpen = true;
        chatFragment = new ChatFragment(userModel, REQUEST_ID, REQUESTER_ID);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, chatFragment).commit();
    }

    private void attachRating() {
        if (userID.equals(Constants.auth().getCurrentUser().getUid())) {
            Toast.makeText(this, "You can't rate yourself", Toast.LENGTH_SHORT).show();
        } else showRatingDialog();
    }

    private void showMap() {
        isChatOpen = false;
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