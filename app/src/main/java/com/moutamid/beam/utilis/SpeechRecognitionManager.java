package com.moutamid.beam.utilis;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechRecognitionManager {
    private static final String TAG = "WelcomeActivity";
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private Context context;
    private Activity activity;
    private RecognitionListener recognitionListener;
    public static int REQUEST_CODE = 1001;

//    public SpeechRecognitionManager(Context context, Activity activity, RecognitionListener recognitionListener) {
//        this.context = context;
//        this.activity = activity;
//        this.recognitionListener = recognitionListener;
//        initializeSpeechRecognizer();
//    }

    public SpeechRecognitionManager(Context context, RecognitionListener listener) {
        this.context = context;
        this.recognitionListener = listener;
        Log.d(TAG, "SpeechRecognitionManager: ");
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(recognitionListener);
//
//        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
//                "com.moutamid.beam");
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
//
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
//        speechRecognizer.setRecognitionListener(listener);
//        recognizer.startListening(intent);

//        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");  // Set your desired language
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
//
//        try {
//            startActivityForResult(activity, speechRecognizerIntent, REQUEST_CODE, null);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(context, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
//        }

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        Log.d(TAG, "initializeSpeechRecognizer: ");
    }

    public void startListening() {
        Log.d(TAG, "startListening: ");
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
        Log.d(TAG, "stopListening: ");
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}

