package com.moutamid.beam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.databinding.FragmentCategoryBinding;
import com.moutamid.beam.models.CategoryModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

import java.util.ArrayList;
import java.util.Comparator;


public class CategoryFragment extends Fragment {
    FragmentCategoryBinding binding;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(getLayoutInflater(), container, false);

        binding.recyler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        binding.recyler.setHasFixedSize(false);

        ArrayList<CategoryModel> category = new ArrayList<>();
        Constants.databaseReference().child(Constants.CATEGORIES).get().addOnSuccessListener(snapshot -> {
            Constants.dismissDialog();
            if (snapshot.exists()) {
                category.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CategoryModel topicsModel = dataSnapshot.getValue(CategoryModel.class);
                    category.add(topicsModel);
                }
                category.sort(Comparator.comparing(categoryModel -> categoryModel.name));
                CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(), category, query -> {
                    RequestModel requestModel = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);
                    if (requestModel == null) requestModel = new RequestModel();
                    requestModel.category = query;
                    Stash.put(Constants.SAVE_REQUEST, requestModel);
                }, true);
                binding.recyler.setAdapter(categoryAdapter);
            }
        });

        return binding.getRoot();
    }
}