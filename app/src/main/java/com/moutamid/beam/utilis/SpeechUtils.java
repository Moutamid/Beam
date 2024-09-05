package com.moutamid.beam.utilis;

import android.speech.SpeechRecognizer;
import android.util.Log;

public interface SpeechUtils {
    void onResult(String result);
    void onError(String error);
}

