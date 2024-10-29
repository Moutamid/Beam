package com.moutamid.beam.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moutamid.beam.databinding.FragmentDocumentsBinding;
import com.moutamid.beam.models.DocumentModel;

import java.io.IOException;

public class DocumentsFragment extends Fragment {
    FragmentDocumentsBinding binding;

    public DocumentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDocumentsBinding.inflate(getLayoutInflater(), container, false);


        return binding.getRoot();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && data != null) {
//            Uri fileUri = data.getData();
//            if (fileUri != null) {
//                try {
//                    // Determine if file size exceeds the 10 MB limit
//                    if (getFileSize(fileUri) > MAX_FILE_SIZE) {
//                        showToast("File size must be less than 10 MB");
//                    } else {
//                        boolean isPickedDocument = (requestCode == PICK_DOCUMENT);
//                        list.add(new DocumentModel(fileUri, isPickedDocument));
//                        updateView();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    showToast("Failed to get file size");
//                }
//            }
//        }
//    }

    // Helper method to get file size from Uri
    private long getFileSize(Uri uri) throws IOException {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();
        return size;
    }

    // Helper method to show a Toast message
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


}