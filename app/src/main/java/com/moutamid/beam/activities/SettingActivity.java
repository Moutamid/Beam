package com.moutamid.beam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;
import com.moutamid.beam.utilis.Stash;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivitySettingBinding;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.title.setText("Settings");
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.edit.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        int DISTANCE = Stash.getInt(Constants.DISTANCE, 0);

        binding.anonymous.setChecked(Stash.getBoolean(Constants.ANONYMOUS, false));

        binding.distance.setValue(DISTANCE);

        binding.anonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Stash.put(Constants.ANONYMOUS, isChecked);
            }
        });

        binding.distance.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (fromUser) Stash.put(Constants.DISTANCE, (int) value);
            }
        });

        binding.logout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        Constants.auth().signOut();
                        startActivity(new Intent(this, SplashActivity.class));
                        finish();
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        binding.name.setText(userModel.name);
        binding.email.setText(userModel.phoneNumber);
    }
}