package com.moutamid.beam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fxn.stash.Stash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.moutamid.beam.MainActivity;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityOtpBinding;
import com.moutamid.beam.models.LocationModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    ActivityOtpBinding binding;
    UserModel userModel;
    String verificationId;
    private static final String TAG = "OtpActivity";
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
            Constants.showDialog();
            verifyCode();
        });

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
                        if (userModel.id == null) {
                            userModel.id = user.getUid();
                            userModel.image = "";
                            userModel.location = new LocationModel();
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
                        } else {
                            Constants.databaseReference().child(Constants.USER).child(user.getUid()).get()
                                    .addOnSuccessListener(dataSnapshot -> {
                                        userModel = dataSnapshot.getValue(UserModel.class);
                                        Stash.put(Constants.STASH_USER, userModel);
                                        Constants.dismissDialog();
                                        startActivity(new Intent(OtpActivity.this, MainActivity.class));
                                        finish();
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
                Log.d(TAG, "onTick: " + millisUntilFinished);
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
            if (code!=null) {
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
            verificationId = s;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
    }
}