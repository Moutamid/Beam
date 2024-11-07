package com.moutamid.beam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moutamid.beam.MainActivity;
import com.moutamid.beam.R;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String requestID = getIntent().getStringExtra("requestID");
        String requesterID = getIntent().getStringExtra("requesterID");
        String userID = getIntent().getStringExtra("userID");

        Log.d(TAG, "requestID: " + requestID);
        Log.d(TAG, "requesterID: " + requesterID);
        Log.d(TAG, "userID: " + userID);

        if (userID != null) {
            if (userID.equals("null")) {
                if (requestID != null && requesterID != null) {
                    Log.d(TAG, "onCreate: GOING FOR THE REQUEST PREVIEW");
                    if (!requestID.isEmpty() && !requesterID.isEmpty()) {
                        Constants.databaseReference().child(Constants.REQUESTS).child(requesterID)
                                .child(requestID).get().addOnSuccessListener(dataSnapshot -> {
                                    if (dataSnapshot.exists()) {
                                        RequestModel requestModel = dataSnapshot.getValue(RequestModel.class);
                                        Stash.put(Constants.PASS_REQUEST, requestModel);
                                        startActivity(new Intent(SplashActivity.this, RequestPreviewActivity.class));
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        startHandler();
                    }
                } else {
                    startHandler();
                }
            } else {
                if (requestID != null && requesterID != null) {
                    if (!requestID.isEmpty() && !requesterID.isEmpty()) {
                        startActivity(new Intent(SplashActivity.this, UserProfileActivity.class)
                                .putExtra("REQUESTER_ID", requesterID)
                                .putExtra("REQUEST_ID", requestID)
                                .putExtra("USER_ID", userID)
                                .putExtra("PREVIEW", false));
                        finish();
                    } else if (requestID.isEmpty()) {
                        startActivity(new Intent(SplashActivity.this, UserProfileActivity.class)
                                .putExtra("REQUESTER_ID", requesterID)
                                .putExtra("REQUEST_ID", requestID)
                                .putExtra("USER_ID", userID)
                                .putExtra("PREVIEW", true));
                        finish();
                    } else {
                        startHandler();
                    }
                } else {
                    startHandler();
                }
            }
        } else {
            startHandler();
        }
    }

    private void startHandler() {
        new Handler().postDelayed(() -> {
            if (Constants.auth().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                finish();
            }
        }, 2000);
    }
}