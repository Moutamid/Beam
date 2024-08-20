package com.moutamid.beam.utilis;

import android.speech.SpeechRecognizer;
import android.util.Log;

public class SpeechUtils {

    public static final String LOG_TAG = "SpeechUtils";
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 100;
    public static final int RESULTS_LIMIT = 1;

    public static final boolean IS_CONTINUES_LISTEN = false;

    public static void errorLog(String msg) {
        Log.e(LOG_TAG, msg);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
                message = "Language Not supported";
                break;
            case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
                message = "Language Unavailable";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}

