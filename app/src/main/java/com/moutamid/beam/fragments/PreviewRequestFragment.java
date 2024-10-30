package com.moutamid.beam.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.ContactsAdapter;
import com.moutamid.beam.adapters.DocumentsAdapter;
import com.moutamid.beam.adapters.MandatoryAdapter;
import com.moutamid.beam.databinding.FragmentPreviewRequestBinding;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class PreviewRequestFragment extends Fragment {

    FragmentPreviewRequestBinding binding;

    public PreviewRequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPreviewRequestBinding.inflate(getLayoutInflater(), container, false);


        UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        Glide.with(this).load(userModel.image).placeholder(R.drawable.profile_icon).into(binding.image);

        RequestModel newRequest = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);

        binding.name.getEditText().setText(newRequest.title);
        binding.category.getEditText().setText(newRequest.category);
        binding.description.setText(newRequest.description);
        binding.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(newRequest.deadline));

        if (newRequest.mandatory != null) {
            MandatoryAdapter adapter = new MandatoryAdapter(requireContext(), newRequest.mandatory);
            binding.mandatoryRC.setAdapter(adapter);
        }

        ArrayList<UserModel> usersList = Stash.getArrayList(Constants.REQUESTERS, UserModel.class);

        if (usersList.isEmpty()) {
            binding.noContact.setVisibility(View.VISIBLE);
            binding.contactRC.setVisibility(View.GONE);
        } else {
            binding.noContact.setVisibility(View.GONE);
            binding.contactRC.setVisibility(View.VISIBLE);
        }

        ContactsAdapter adapter = new ContactsAdapter(requireContext(), usersList, null);
        binding.contactRC.setAdapter(adapter);


        ArrayList<DocumentModel> list = Stash.getArrayList(Constants.DOCUMENTS, DocumentModel.class);

        if (list.isEmpty()) {
            binding.documentsRC.setVisibility(View.GONE);
            binding.noDocument.setVisibility(View.VISIBLE);
        } else {
            binding.documentsRC.setVisibility(View.VISIBLE);
            binding.noDocument.setVisibility(View.GONE);
        }

        DocumentsAdapter documentsAdapter = new DocumentsAdapter(requireContext(), list, pos -> {});
        binding.documentsRC.setAdapter(documentsAdapter);

        return binding.getRoot();
    }

}