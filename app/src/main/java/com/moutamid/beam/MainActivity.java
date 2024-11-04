package com.moutamid.beam;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.moutamid.beam.activities.ActiveOrdersActivity;
import com.moutamid.beam.activities.ManageCategoryActivity;
import com.moutamid.beam.activities.NewRequestActivity;
import com.moutamid.beam.activities.SettingActivity;
import com.moutamid.beam.adapters.CategoriesAdapter;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.adapters.RequestsAdapter;
import com.moutamid.beam.databinding.ActivityMainBinding;
import com.moutamid.beam.models.CategoryModel;
import com.moutamid.beam.models.LocationModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    boolean isSearchEnable = false;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";
    UserModel userModel;
    private FusedLocationProviderClient fusedLocationClient;
    ArrayList<RequestModel> list;
    RequestsAdapter adapter;

    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;

    private void requestMissingPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };

            if (
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED
            ) {
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.VIBRATE);
                shouldShowRequestPermissionRationale(Manifest.permission.WAKE_LOCK);

                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };

            if (
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED
            ) {
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.VIBRATE);
                shouldShowRequestPermissionRationale(Manifest.permission.WAKE_LOCK);

                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        } else {
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            };

            if (
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED
            ) {
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.VIBRATE);
                shouldShowRequestPermissionRationale(Manifest.permission.WAKE_LOCK);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);

                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        list = new ArrayList<>();

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            Log.d("NotificationHelper", "getToken: " + s);
            Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid()).child("fcmToken").setValue(s);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });

        requestMissingPermissions();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSearchEnable) {
                    binding.toolbar.nameLayout.setVisibility(View.VISIBLE);
                    binding.toolbar.search.setVisibility(View.VISIBLE);
                    binding.toolbar.searchLayout.setVisibility(View.GONE);
                    isSearchEnable = false;
                } else {
                    try {
                        getOnBackPressedDispatcher().onBackPressed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        binding.toolbar.search.setOnClickListener(v -> {
            showSearchLayout();
        });

        binding.toolbar.back.setOnClickListener(v -> {
            hideSearchLayout();
        });

        binding.toolbar.searchBtn.setOnClickListener(v -> {
            filterItem();
        });

        binding.toolbar.more.setOnClickListener(v -> {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = inflater.inflate(R.layout.buttons, null);
            PopupWindow popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(v);
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1]);
            MaterialButton profile = customView.findViewById(R.id.profile);
            MaterialButton settings = customView.findViewById(R.id.settings);
            MaterialButton cat = customView.findViewById(R.id.category);

            if (Constants.auth().getCurrentUser().getUid().equals(Constants.ADMIN_ID)) {
                cat.setVisibility(View.VISIBLE);
            } else cat.setVisibility(View.GONE);

            profile.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, ActiveOrdersActivity.class));
            });

            settings.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, SettingActivity.class));
            });

            cat.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, ManageCategoryActivity.class));
            });
        });

        binding.toolbar.newResponse.setOnClickListener(v -> {
            Stash.clear(Constants.SAVE_REQUEST);
            Stash.clear(Constants.DOCUMENTS);
            Stash.clear(Constants.REQUESTERS);
            startActivity(new Intent(MainActivity.this, NewRequestActivity.class));
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setHasFixedSize(false);

        binding.mic.listen.setOnClickListener(v -> {
            if (listeningAnimation == null || !listeningAnimation.isRunning()) {
                listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                speechRecognitionManager.startListening();
            } else {
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
                listeningAnimation = null;
                speechRecognitionManager.stopListening();
            }
        });

    }

    private void hideSearchLayout() {
        binding.toolbar.nameLayout.setVisibility(View.VISIBLE);
        binding.toolbar.search.setVisibility(View.VISIBLE);
        binding.toolbar.newResponse.setVisibility(View.VISIBLE);
        binding.toolbar.searchLayout.setVisibility(View.GONE);
        isSearchEnable = false;

        adapter.getFilter().filter("");
    }

    SpeechUtils speechUtils = new SpeechUtils() {
        @Override
        public void onResult(String result) {
            Log.d(TAG, "onResult: " + result);
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            if (result.toLowerCase(Locale.ROOT).contains("close app") || result.toLowerCase(Locale.ROOT).contains("close the app") ||
                    result.toLowerCase(Locale.ROOT).contains("close this app") ||
                    result.toLowerCase(Locale.ROOT).contains("go back")) {
                if (isSearchEnable) {
                    hideSearchLayout();
                } else {
                    MainActivity.this.finish();
                }
            } else if (result.toLowerCase(Locale.ROOT).contains("more") || result.toLowerCase(Locale.ROOT).contains("settings")) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            } else if (result.toLowerCase(Locale.ROOT).contains("search")) {
                if (!isSearchEnable) {
                    showSearchLayout();
                } else {
                    filterItem();
                }
            } else if (result.toLowerCase(Locale.ROOT).contains("new") || result.toLowerCase(Locale.ROOT).contains("add new")
                    || result.toLowerCase(Locale.ROOT).contains("add") || result.toLowerCase(Locale.ROOT).contains("create new")
                    || result.toLowerCase(Locale.ROOT).contains("new request")) {
                startActivity(new Intent(MainActivity.this, NewRequestActivity.class));
            } else {
                if (isSearchEnable) {
                    binding.toolbar.searchEt.getEditText().setText(result);
                }
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    private void filterItem() {
        String query = binding.toolbar.searchEt.getEditText().getText().toString().trim();
        adapter.getFilter().filter(query);
    }

    private void showSearchLayout() {
        binding.toolbar.nameLayout.setVisibility(View.GONE);
        binding.toolbar.search.setVisibility(View.GONE);
        binding.toolbar.newResponse.setVisibility(View.GONE);
        binding.toolbar.searchLayout.setVisibility(View.VISIBLE);
        isSearchEnable = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//            new Handler().postDelayed(() -> {
//                runOnUiThread(() -> {
//                    Speech.init(this, getPackageName());
//                    speechRecognitionManager = new SpeechRecognitionManager(this, speechUtils);
//                    listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
//                    speechRecognitionManager.startListening();
//                });
//            }, 1000);
//        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Log.d(TAG, "onCreate: " + location.getLatitude());
                            Log.d(TAG, "onCreate: " + location.getLongitude());
                            updateLocation(location);
                        } else {
                            new MaterialAlertDialogBuilder(this)
                                    .setMessage("This function requires a gps connection")
                                    .setCancelable(false)
                                    .setPositiveButton("Open Settings", (dialog, which) -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(intent);
                                    }).setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    });
        }

        ArrayList<CategoryModel> category = new ArrayList<>();
        Constants.databaseReference().child(Constants.CATEGORIES).get().addOnSuccessListener(snapshot -> {
            Constants.dismissDialog();
            if (snapshot.exists()) {
                category.clear();
                category.add(0, new CategoryModel("ALL", "All"));
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CategoryModel topicsModel = dataSnapshot.getValue(CategoryModel.class);
                    category.add(topicsModel);
                }
                category.sort(Comparator.comparing(categoryModel -> categoryModel.name));
                CategoryAdapter categoryAdapter = new CategoryAdapter(this, category, query -> {
                    if (adapter != null) {
                        if (query.equals("All")) adapter.getFilter().filter("");
                        else adapter.getFilter().filter(query);

                        if (adapter.getItemCount() == 0) {
                            binding.noLayout.setVisibility(View.VISIBLE);
                            binding.recycler.setVisibility(View.GONE);
                        } else {
                            binding.noLayout.setVisibility(View.GONE);
                            binding.recycler.setVisibility(View.VISIBLE);
                        }
                    }
                }, false);
                binding.categoryRC.setAdapter(categoryAdapter);
                getList();
            }
        });
    }

    private void getList() {
        Constants.showDialog();
        Constants.databaseReference().child(Constants.REQUESTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Constants.dismissDialog();
                        list = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                    RequestModel requestModel = dataSnapshot2.getValue(RequestModel.class);
                                    list.add(requestModel);
                                }
                            }
                        }

                        if (list.isEmpty()) {
                            binding.noLayout.setVisibility(View.VISIBLE);
                            binding.recycler.setVisibility(View.GONE);
                        } else {
                            binding.noLayout.setVisibility(View.GONE);
                            binding.recycler.setVisibility(View.VISIBLE);
                        }

                        adapter = new RequestsAdapter(MainActivity.this, list);
                        binding.recycler.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission granted");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateLocation(location);
                        } else {
                            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateLocation(Location location) {
        userModel.location = new LocationModel(location.getLatitude(), location.getLongitude());
        Map<String, Object> loc = new HashMap<>();
        loc.put("location", userModel.location);
        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .updateChildren(loc).addOnSuccessListener(unused -> {
                    Stash.put(Constants.STASH_USER, userModel);
                });

    }
}