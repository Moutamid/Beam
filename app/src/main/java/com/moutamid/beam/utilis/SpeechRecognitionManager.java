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

import com.moutamid.beam.TestActivity;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import java.util.ArrayList;
import java.util.List;

public class SpeechRecognitionManager {
    private static final String TAG = "SpeechRecognitionManager";
    private Context context;
    SpeechUtils speechUtils;

    public SpeechRecognitionManager(Context context, SpeechUtils speechUtils) {
        this.context = context;
        this.speechUtils = speechUtils;
    }

    public void startListening() {
        Log.d(TAG, "startListening: ");
        try {
            Speech.getInstance().startListening(new SpeechDelegate() {
                @Override
                public void onStartOfSpeech() {
                    Log.i(TAG, "speech recognition is now active");
                }

                @Override
                public void onSpeechRmsChanged(float value) {
                  //  Log.d(TAG, "rms is now: " + value);
                }

                @Override
                public void onSpeechPartialResults(List<String> results) {
                    StringBuilder str = new StringBuilder();
                    for (String res : results) {
                        str.append(res).append(" ");
                    }
                    Log.i(TAG, "partial result: " + str.toString().trim());
                }

                @Override
                public void onSpeechResult(String result) {
                    speechUtils.onResult(result);
                }
            });
        } catch (SpeechRecognitionNotAvailable exc) {
            speechUtils.onError("Speech recognition is not available on this device!");
            Log.e(TAG, "Speech recognition is not available on this device!");
        } catch (GoogleVoiceTypingDisabledException exc) {
            speechUtils.onError("Google voice typing must be enabled!");
            Log.e(TAG, "Google voice typing must be enabled!");
        }
    }

    public void stopListening() {
        try {
            Speech.getInstance().stopListening();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "stopListening: " + e.getLocalizedMessage());
        }
    }

    public void destroy() {
        try {
            Speech.getInstance().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "stopListening: " + e.getLocalizedMessage());
        }
    }
}

