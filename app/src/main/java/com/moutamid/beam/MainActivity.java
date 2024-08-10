package com.moutamid.beam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.moutamid.beam.activities.SettingActivity;
import com.moutamid.beam.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    boolean isSearchEnable = false;
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
            binding.toolbar.searchLayout.setVisibility(View.VISIBLE);
            isSearchEnable = true;
        });

        binding.toolbar.back.setOnClickListener(v -> {
            binding.toolbar.nameLayout.setVisibility(View.VISIBLE);
            binding.toolbar.search.setVisibility(View.VISIBLE);
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

    }
}