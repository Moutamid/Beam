package com.moutamid.beam.activities;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.CategoriesAdapter;
import com.moutamid.beam.databinding.ActivityManageCategoryBinding;
import com.moutamid.beam.models.CategoryModel;
import com.moutamid.beam.utilis.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class ManageCategoryActivity extends AppCompatActivity {
    ActivityManageCategoryBinding binding;
    ArrayList<CategoryModel> list;
    CategoriesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.title.setText("Categories");
        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        binding.toolbar.addNew.setVisibility(View.VISIBLE);

        list = new ArrayList<>();

        binding.topics.setLayoutManager(new LinearLayoutManager(this));
        binding.topics.setHasFixedSize(false);

        binding.toolbar.addNew.setOnClickListener(v -> showDialog());

        binding.search.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_category);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();

        TextInputLayout topic = dialog.findViewById(R.id.topic);
        Button complete = dialog.findViewById(R.id.complete);

        complete.setOnClickListener(v -> {
            String topicName = topic.getEditText().getText().toString().trim();
            if (!topicName.isEmpty()) {
                dialog.dismiss();
                Constants.showDialog();
                CategoryModel model = new CategoryModel(UUID.randomUUID().toString(), topicName);
                Constants.databaseReference().child(Constants.CATEGORIES).child(model.id).setValue(model)
                        .addOnSuccessListener(unused -> {
                            Constants.dismissDialog();
                            Toast.makeText(ManageCategoryActivity.this, "Category Added Successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Constants.dismissDialog();
                            Toast.makeText(ManageCategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                topic.setErrorEnabled(true);
                topic.setError("Category name is empty");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        Constants.showDialog();

        Constants.databaseReference().child(Constants.CATEGORIES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Constants.dismissDialog();
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CategoryModel topicsModel = dataSnapshot.getValue(CategoryModel.class);
                        list.add(topicsModel);
                    }
                    list.sort(Comparator.comparing(categoryModel -> categoryModel.name));
                    adapter = new CategoriesAdapter(ManageCategoryActivity.this, list);
                    binding.topics.setAdapter(adapter);
                } else {
                    adapter = new CategoriesAdapter(ManageCategoryActivity.this, new ArrayList<>());
                    binding.topics.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Constants.dismissDialog();
                Toast.makeText(ManageCategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}