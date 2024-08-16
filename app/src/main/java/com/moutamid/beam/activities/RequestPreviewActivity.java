package com.moutamid.beam.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.adapters.DocumentsAdapter;
import com.moutamid.beam.databinding.ActivityRequestPreviewBinding;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestPreviewActivity extends AppCompatActivity {
    ActivityRequestPreviewBinding binding;
    RequestModel requestModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestModel = (RequestModel) Stash.getObject(Constants.PASS_REQUEST, RequestModel.class);

        binding.toolbar.title.setText("Preview");
        binding.toolbar.stop.setVisibility(View.VISIBLE);
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.reply.setOnClickListener(v -> {
            startActivity(new Intent(this, RequestResponseActivity.class));
        });

        binding.name.setText(requestModel.title);
        binding.description.setText(requestModel.description);
        binding.postTime.setText("Posted " + Constants.getTime(requestModel.timestamp));
        binding.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(requestModel.deadline));

        Constants.databaseReference().child(Constants.USER).child(requestModel.userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (!isDestroyed()) {
                            binding.username.setText(userModel.name);
                            Glide.with(RequestPreviewActivity.this).load(userModel.image).placeholder(R.drawable.profile_icon).into(binding.image);

                            if (userModel.status) {
                                binding.status.setBackgroundTintList(ColorStateList.valueOf(RequestPreviewActivity.this.getColor(R.color.green)));
                            } else {
                                binding.status.setBackgroundTintList(ColorStateList.valueOf(RequestPreviewActivity.this.getColor(R.color.stroke)));
                            }

                            if (userModel.rating != null) {
                                float rating = 0;
                                for (double commentModel : userModel.rating) rating += commentModel;
                                float total = rating / userModel.rating.size();
                                String rate = String.format(Locale.getDefault(), "%.2f", total) + " (" + userModel.rating.size() + ")";
                                if (userModel.rating.size() > 1) binding.rating.setText(rate);
                                else binding.rating.setText(userModel.rating.get(0) + " (1)");
                            } else {
                                binding.rating.setText("0.0 (0)");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.containDocument.setVisibility(View.GONE);
        binding.containImage.setVisibility(View.GONE);

        if (requestModel.documents != null) {
            if (!requestModel.documents.isEmpty()) {
                boolean hasDoc = requestModel.documents.stream().anyMatch(doc -> doc.isDoc);
                boolean hasNonDoc = requestModel.documents.stream().anyMatch(doc -> !doc.isDoc);
                if (hasDoc) binding.containDocument.setVisibility(View.VISIBLE);
                if (hasNonDoc) binding.containImage.setVisibility(View.VISIBLE);
            }
        }

        if (requestModel.mandatory != null) {
            CategoryAdapter categoryAdapter = new CategoryAdapter(this, requestModel.mandatory, null);
            binding.madatoryItems.setAdapter(categoryAdapter);
        }
    }
}