package com.moutamid.beam;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moutamid.beam.utilis.Stash;
import com.moutamid.beam.utilis.Constants;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "MyApp";
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.d(TAG, "onActivityResumed: ");
        updateStatus(true);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.d(TAG, "onActivityPaused: ");
        updateStatus(false);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    private void updateStatus(boolean status) {
        if (Constants.auth().getCurrentUser() != null) {
            Map<String, Object> loc = new HashMap<>();
            loc.put("status", status);
            Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                    .updateChildren(loc);
        }
    }
}
