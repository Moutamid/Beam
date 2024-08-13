package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    ActivityWelcomeBinding binding;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);

        speechRecognitionManager = new SpeechRecognitionManager(this, new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
//                "com.moutamid.beam");
//
//        SpeechRecognizer recognizer = SpeechRecognizer
//                .createSpeechRecognizer(this.getApplicationContext());
//
//        RecognitionListener listener = new RecognitionListener() {
//            @Override
//            public void onResults(Bundle results) {
//                ArrayList<String> voiceResults = results
//                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                if (voiceResults == null) {
//                    System.out.println("No voice results");
//                } else {
//                    System.out.println("Printing matches: ");
//                    for (String match : voiceResults) {
//                        System.out.println(match);
//                    }
//                }
//            }
//
//            @Override
//            public void onReadyForSpeech(Bundle params) {
//                System.out.println("Ready for speech");
//            }
//
//            /**
//             *  ERROR_NETWORK_TIMEOUT = 1;
//             *  ERROR_NETWORK = 2;
//             *  ERROR_AUDIO = 3;
//             *  ERROR_SERVER = 4;
//             *  ERROR_CLIENT = 5;
//             *  ERROR_SPEECH_TIMEOUT = 6;
//             *  ERROR_NO_MATCH = 7;
//             *  ERROR_RECOGNIZER_BUSY = 8;
//             *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
//             *
//             * @param error code is defined in SpeechRecognizer
//             */
//            @Override
//            public void onError(int error) {
//                System.err.println("Error listening for speech: " + error);
//            }
//
//            @Override
//            public void onBeginningOfSpeech() {
//                System.out.println("Speech starting");
//            }
//
//            @Override
//            public void onBufferReceived(byte[] buffer) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onEndOfSpeech() {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onEvent(int eventType, Bundle params) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onPartialResults(Bundle partialResults) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onRmsChanged(float rmsdB) {
//                // TODO Auto-generated method stub
//
//            }
//        };
//        recognizer.setRecognitionListener(listener);
    //    recognizer.startListening(intent);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            speechRecognitionManager.startListening();

        binding.mic.listen.setOnClickListener(v -> {
            if (listeningAnimation == null || !listeningAnimation.isRunning()) {
                listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                speechRecognitionManager.startListening();
                Log.d(TAG, "onCreate: START");
//                recognizer.startListening(intent);
            } else {
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
                listeningAnimation = null;
                speechRecognitionManager.stopListening();
                Log.d(TAG, "onCreate: END");
//                recognizer.stopListening();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechRecognitionManager.startListening();
            } else {
                Toast.makeText(this, "Permission for recording audio was denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechRecognitionManager.REQUEST_CODE) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);
            Log.d(TAG, "onActivityResult: " + spokenText);
        }
    }
}