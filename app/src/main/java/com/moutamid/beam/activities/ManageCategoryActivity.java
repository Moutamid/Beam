package com.moutamid.beam.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityManageCategoryBinding;

public class ManageCategoryActivity extends AppCompatActivity {
    ActivityManageCategoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}