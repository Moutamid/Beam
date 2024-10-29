package com.moutamid.beam;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

import java.util.ArrayList;
import java.util.Calendar;
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
        checkDeadline();
    }

    private void checkDeadline() {
        Constants.databaseReference().child(Constants.REQUESTS)
                .get().addOnSuccessListener(snapshot -> {
                    ArrayList<RequestModel> list = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                RequestModel requestModel = dataSnapshot2.getValue(RequestModel.class);
                                list.add(requestModel);
                            }
                        }

                        for (RequestModel requests : list) {
                            long currentTime = System.currentTimeMillis();
                            Calendar timestampCal = Calendar.getInstance();
                            timestampCal.setTimeInMillis(requests.deadline);

                            Calendar currentCal = Calendar.getInstance();
                            currentCal.setTimeInMillis(currentTime);
                            timestampCal.set(Calendar.HOUR_OF_DAY, 0);
                            timestampCal.set(Calendar.MINUTE, 0);
                            timestampCal.set(Calendar.SECOND, 0);
                            timestampCal.set(Calendar.MILLISECOND, 0);

                            currentCal.set(Calendar.HOUR_OF_DAY, 0);
                            currentCal.set(Calendar.MINUTE, 0);
                            currentCal.set(Calendar.SECOND, 0);
                            currentCal.set(Calendar.MILLISECOND, 0);

                            if (currentCal.after(timestampCal)) {
                                Constants.databaseReference().child(Constants.REQUESTS).child(requests.userID).child(requests.ID)
                                        .removeValue();

                                Constants.databaseReference().child(Constants.REQUESTS_REPLY).child(requests.ID)
                                        .removeValue();
                            } else {
                                System.out.println("The date has not yet passed.");
                            }
                        }
                    }
                });
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
