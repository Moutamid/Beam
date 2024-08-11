package com.moutamid.beam.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityUserProfileBinding;
import com.moutamid.beam.fragments.ChatFragment;
import com.moutamid.beam.fragments.MapFragment;

public class UserProfileActivity extends AppCompatActivity {
    ActivityUserProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbar.title.setText("user");
        binding.toolbar.stop.setVisibility(View.VISIBLE);
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MapFragment()).commit();

        binding.map.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new MapFragment()).commit();
        });

        binding.chat.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChatFragment()).commit();
        });

    }
}