package com.moutamid.beam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.moutamid.beam.activities.NewRequestActivity;
import com.moutamid.beam.activities.SettingActivity;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.adapters.RequestsAdapter;
import com.moutamid.beam.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    boolean isSearchEnable = false;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";

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
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<String> category = new ArrayList<>();
        category.add("Automobile");
        category.add("Food");
        category.add("Plumbing");
        category.add("Carpenter");
        category.add("Mechanic");

        CategoryAdapter categoryAdapter = new CategoryAdapter(this, category);
        binding.categoryRC.setAdapter(categoryAdapter);

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
            binding.toolbar.nameLayout.setVisibility(View.GONE);
            binding.toolbar.search.setVisibility(View.GONE);
            binding.toolbar.newResponse.setVisibility(View.GONE);
            binding.toolbar.searchLayout.setVisibility(View.VISIBLE);
            isSearchEnable = true;
        });

        binding.toolbar.back.setOnClickListener(v -> {
            binding.toolbar.nameLayout.setVisibility(View.VISIBLE);
            binding.toolbar.search.setVisibility(View.VISIBLE);
            binding.toolbar.newResponse.setVisibility(View.VISIBLE);
            binding.toolbar.searchLayout.setVisibility(View.GONE);
            isSearchEnable = false;
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

            profile.setOnClickListener(v1 -> {
                popupWindow.dismiss();
            });
            settings.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, SettingActivity.class));
            });
        });

        binding.toolbar.newResponse.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NewRequestActivity.class));
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setHasFixedSize(false);

        RequestsAdapter adapter = new RequestsAdapter(this);
        binding.recycler.setAdapter(adapter);

    }

    private void requestLocationPermission() {
        shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.POST_NOTIFICATIONS}, 111);
    }

}