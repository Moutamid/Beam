package com.moutamid.beam.notification;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private Context context;

    public TokenManager(Context context) {
        this.context = context;
    }

    public String getAccessToken() {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream serviceAccount = assetManager.open("serviceAccountKey.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            if (token != null) {
                return token.getTokenValue();
            } else {
                Log.d(TAG, "AccessToken is null");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting access token", e);
            return null;
        }
    }

}

