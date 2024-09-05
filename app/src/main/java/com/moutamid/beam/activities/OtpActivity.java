package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.moutamid.beam.MainActivity;
import com.moutamid.beam.databinding.ActivityOtpBinding;
import com.moutamid.beam.models.LocationModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    ActivityOtpBinding binding;
    UserModel userModel;
    String verificationId;
    private static final String TAG = "OtpActivity";

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
            } else if (result.toLowerCase(Locale.ROOT).contains("verify")) {
                verify();
            }  else if (result.toLowerCase(Locale.ROOT).contains("resend")) {
                login();
            } else if (result.toLowerCase(Locale.ROOT).contains("select otp")) {
                binding.otp.getEditText().requestFocus();
            } else {
                if (binding.otp.getEditText().hasFocus()) {
                    binding.otp.getEditText().setText(result.replace(" ", "").replace("-", ""));
                }
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(OtpActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);

        Log.d(TAG, "onCreate: " + (userModel.id == null));

        binding.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.number.setText("We sent a otp on this number : " + userModel.phoneNumber);

        login();

        binding.resend.setOnClickListener(v -> {
            login();
        });

        binding.create.setOnClickListener(v -> {
            verify();
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

    private void verify() {
        if (!binding.otp.getEditText().getText().toString().isEmpty() && verificationId != null) {
            Constants.showDialog();
            verifyCode();
        } else {
            Toast.makeText(this, "OTP is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, binding.otp.getEditText().getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Constants.auth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (userModel.name != null) {
                            userModel.id = user.getUid();
                            userModel.image = "";
                            userModel.location = new LocationModel();

                            Constants.databaseReference().child(Constants.USER).child(user.getUid()).get()
                                    .addOnSuccessListener(dataSnapshot -> {
                                        if (dataSnapshot.exists()) {
                                            Constants.auth().signOut();
                                            getOnBackPressedDispatcher().onBackPressed();
                                            Toast.makeText(this, "User Already Exist", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Constants.databaseReference().child(Constants.USER).child(userModel.id).setValue(userModel)
                                                    .addOnSuccessListener(unused -> {
                                                        Stash.put(Constants.STASH_USER, userModel);
                                                        Constants.dismissDialog();
                                                        startActivity(new Intent(OtpActivity.this, MainActivity.class));
                                                        finish();
                                                    }).addOnFailureListener(e -> {
                                                        Constants.dismissDialog();
                                                        Toast.makeText(OtpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }).addOnFailureListener(e -> {
                                        Constants.dismissDialog();
                                        Toast.makeText(OtpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Constants.databaseReference().child(Constants.USER).child(user.getUid()).get()
                                    .addOnSuccessListener(dataSnapshot -> {
                                        if (dataSnapshot.exists()) {
                                            userModel = dataSnapshot.getValue(UserModel.class);
                                            Stash.put(Constants.STASH_USER, userModel);
                                            Constants.dismissDialog();
                                            startActivity(new Intent(OtpActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Constants.auth().signOut();
                                            getOnBackPressedDispatcher().onBackPressed();
                                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                        Constants.dismissDialog();
                                        Toast.makeText(OtpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(OtpActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                        }
                        Constants.dismissDialog();
                    }
                });
    }

    private void login() {
        // 120_000 (2 minutes)
        new CountDownTimer(120_000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String time = new SimpleDateFormat("mm:ss", Locale.getDefault()).format(millisUntilFinished);
                binding.timer.setText(time);
            }

            @Override
            public void onFinish() {
                binding.resend.setEnabled(true);
            }
        }.start();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(Constants.auth())
                        .setPhoneNumber(userModel.phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        Constants.auth().useAppLanguage();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential c) {
            final String code = c.getSmsCode();
            Log.d(TAG, "onVerificationCompleted: ");
            if (code != null) {
                binding.otp.getEditText().setText(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }
            Toast.makeText(OtpActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onVerificationFailed: " + e.getLocalizedMessage());
            e.printStackTrace();
            getOnBackPressedDispatcher().onBackPressed();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            Log.d(TAG, "onCodeSent: ");
            Toast.makeText(OtpActivity.this, "Verification Sent", Toast.LENGTH_SHORT).show();
            verificationId = s;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
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