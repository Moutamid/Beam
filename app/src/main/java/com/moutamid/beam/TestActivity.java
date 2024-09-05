package com.moutamid.beam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moutamid.beam.databinding.ActivityTestBinding;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {
    ActivityTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        startListening();

        checkPermissions();

        Speech.init(this, getPackageName());

        binding.btnStartListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Speech.getInstance().startListening(new SpeechDelegate() {
                        @Override
                        public void onStartOfSpeech() {
                            Log.i("speech", "speech recognition is now active");
                        }

                        @Override
                        public void onSpeechRmsChanged(float value) {
                            Log.d("speech", "rms is now: " + value);
                        }

                        @Override
                        public void onSpeechPartialResults(List<String> results) {
                            StringBuilder str = new StringBuilder();
                            for (String res : results) {
                                str.append(res).append(" ");
                            }

                            Log.i("speech", "partial result: " + str.toString().trim());
                        }

                        @Override
                        public void onSpeechResult(String result) {
                            Log.i("speech", "result: " + result);
                        }
                    });
                } catch (SpeechRecognitionNotAvailable exc) {
                    Toast.makeText(TestActivity.this, "Speech recognition is not available on this device!", Toast.LENGTH_SHORT).show();
                    Log.e("speech", "Speech recognition is not available on this device!");
                } catch (GoogleVoiceTypingDisabledException exc) {
                    Toast.makeText(TestActivity.this, "Google voice typing must be enabled!", Toast.LENGTH_SHORT).show();
                    Log.e("speech", "Google voice typing must be enabled!");
                }
            }
        });


//        mContext = this;

//        setListeners();
//        checkPermissions();
//        resetSpeechRecognizer();
//        setRecogniserIntent();
//        prepareLocales();

    }

    private void setListeners() {

    }

    private void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    TestActivity.this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    1001
            );
        }
    }

//    private void resetSpeechRecognizer() {
//        if (speechRecognizer != null) speechRecognizer.destroy();
//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
//        errorLog("isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(mContext));
//        if (SpeechRecognizer.isRecognitionAvailable(mContext)) {
//            speechRecognizer.setRecognitionListener(mRecognitionListener);
//        } else {
//            Toast.makeText(mContext, "Speech Recognizer is not available", Toast.LENGTH_SHORT).show();
//        }
//    }

//    private void setRecogniserIntent() {
//        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RESULTS_LIMIT);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startListening();
//            } else {
//                Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }
//
//    private void startListening() {
//        speechRecognizer.startListening(recognizerIntent);
//        binding.progressBar1.setVisibility(View.VISIBLE);
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        errorLog("resume");
//       // resetSpeechRecognizer();
//        if (IS_CONTINUES_LISTEN) {
//            startListening();
//        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        errorLog("pause");
//        if (speechRecognizer != null) {
//            speechRecognizer.stopListening();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        errorLog("stop");
//        if (speechRecognizer != null) {
//            speechRecognizer.destroy();
//        }
    }
/*
    private void prepareLocales() {
        final Locale[] availableLocales = Locale.getAvailableLocales();
        ArrayAdapter<Object> adapterLocalization = new ArrayAdapter<>(
                mContext, android.R.layout.simple_spinner_item, availableLocales);
        adapterLocalization.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguage = availableLocales[position].toString();
                resetSpeechRecognizer();
                setRecogniserIntent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: Handle this case if needed
            }
        });

        binding.spinner1.setAdapter(adapterLocalization);

        // Set "en" as selected language by default
        for (int i = 0; i < availableLocales.length; i++) {
            Locale locale = availableLocales[i];
            if (locale.toString().equalsIgnoreCase("en")) {
                binding.spinner1.setSelection(i);
                break;
            }
        }
    }

    private final RecognitionListener mRecognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            errorLog("onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            errorLog("onBeginningOfSpeech");
            binding.progressBar1.setIndeterminate(false);
            binding.progressBar1.setMax(10);
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            binding.progressBar1.setProgress((int) rmsdB);
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            errorLog("onBufferReceived: " + buffer);
        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            String errorMessage = getErrorText(error);
            errorLog("FAILED " + errorMessage);
            binding.tvError.setText(errorMessage);

            resetSpeechRecognizer();
            startListening();
        }

        @Override
        public void onResults(Bundle results) {
            errorLog("onResults");
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            StringBuilder text = new StringBuilder();
            for (String result : matches) {
                text.append(result).append("\n");
            }
            binding.textView1.setText(text.toString());
            if (IS_CONTINUES_LISTEN) {
                startListening();
            } else {
                binding.progressBar1.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            errorLog("onPartialResults");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            errorLog("onEvent");
        }
    };*/

}