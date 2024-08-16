package com.moutamid.beam.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityEditProfileBinding;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;
    UserModel userModel;
    Uri imageURI;
    private static final int PICK_FROM_GALLERY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        binding.name.getEditText().setText(userModel.name);
        binding.category.getEditText().setText(userModel.category);
        Glide.with(this).load(userModel.image).placeholder(R.drawable.profile_icon).into(binding.profile);

        binding.profile.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(PICK_FROM_GALLERY);
        });

        binding.update.setOnClickListener(v -> {
            if (valid()) {
                Constants.showDialog();
                if (imageURI == null) {
                    uploadData(userModel.image);
                } else {
                    uploadImage();
                }
            }
        });

        String[] service_categories = getResources().getStringArray(R.array.service_categories);
        ArrayAdapter<String> subject = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, service_categories);
        binding.categoryList.setAdapter(subject);
    }

    private void uploadImage() {
        Constants.storageReference(Constants.auth().getCurrentUser().getUid()).child("images").child(new SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault()).format(new Date())).putFile(imageURI)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadData(uri.toString());
                    });
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean valid() {
        if (binding.name.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.category.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Role is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadData(String img) {
        userModel.image = img;
        userModel.name = binding.name.getEditText().getText().toString().trim();
        userModel.category = binding.category.getEditText().getText().toString().trim();

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid()).setValue(userModel)
                .addOnSuccessListener(unused -> {
                    Constants.dismissDialog();
                    Stash.put(Constants.STASH_USER, userModel);
                    getOnBackPressedDispatcher().onBackPressed();
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            Glide.with(this).load(imageURI).placeholder(R.drawable.profile_icon).into(binding.profile);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
    }
}