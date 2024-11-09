package com.moutamid.beam.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.beam.activities.NewRequestActivity;
import com.moutamid.beam.activities.UserProfileActivity;
import com.moutamid.beam.adapters.ContactsAdapter;
import com.moutamid.beam.adapters.DocumentsAdapter;
import com.moutamid.beam.adapters.MandatoryAdapter;
import com.moutamid.beam.databinding.FragmentDocumentsBinding;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class DocumentsFragment extends Fragment {
    FragmentDocumentsBinding binding;
    private static final int PICK_FROM_CAMERA = 1001;
    private static final int PICK_FROM_GALLERY = 1002;
    private static final int PICK_DOCUMENT = 1003;
    RequestModel requestModel;
    ArrayList<DocumentModel> list;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    private static final String TAG = "DocumentsFragment";

    private ActivityResultLauncher<Intent> pickDocumentLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    SpeechUtils speechUtils = new SpeechUtils() {
        @Override
        public void onResult(String result) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Log.d(TAG, "onResult: " + result);
            if (result.toLowerCase(Locale.ROOT).contains("attach document")) {
                attachDocument();
            } else if (result.toLowerCase(Locale.ROOT).contains("attach image") || result.toLowerCase(Locale.ROOT).contains("open gallery")) {
                attachImage();
            }  else if (result.toLowerCase(Locale.ROOT).contains("attach image from camera") || result.toLowerCase(Locale.ROOT).contains("open camera")) {
                attachCamera();
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        }
    };

    public DocumentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            try {
                                // Determine if file size exceeds the 20 MB limit
                                if (getFileSize(fileUri) > MAX_FILE_SIZE) {
                                    showToast("File size must be less than 20 MB");
                                } else {
                                    list.add(new DocumentModel(fileUri.toString(), true));
                                    updateView();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast("Failed to get file size");
                            }
                        }
                    }
                }
        );
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            try {
                                // Determine if file size exceeds the 20 MB limit
                                if (getFileSize(fileUri) > MAX_FILE_SIZE) {
                                    showToast("File size must be less than 20 MB");
                                } else {
                                    list.add(new DocumentModel(fileUri.toString(), false));
                                    updateView();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast("Failed to get file size");
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDocumentsBinding.inflate(getLayoutInflater(), container, false);

        list = Stash.getArrayList(Constants.DOCUMENTS, DocumentModel.class);

        requestModel = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);

        Log.d(TAG, "requestModel.category: " + requestModel.category);

        ArrayList<UserModel> usersList = new ArrayList<>();
        Constants.databaseReference().child(Constants.USER).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (Constants.auth().getCurrentUser() != null) {
                        if (user != null) {
                            if (!user.id.equals(Constants.auth().getCurrentUser().getUid()) && user.category.equals(requestModel.category)) {
                                int DISTANCE = Stash.getInt(Constants.DISTANCE, 0);
                                if (DISTANCE != 0) {
                                    UserModel currentUser = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
                                    double distance = Constants.calculateDistance(currentUser.location.lat, currentUser.location.log, user.location.lat, user.location.log);
                                    if (distance <= DISTANCE) {
                                        usersList.add(user);
                                    }
                                } else {
                                    usersList.add(user);
                                }
                            }
                        }
                    }
                }
            }
            if (usersList.isEmpty()) {
                binding.noContact.setVisibility(View.VISIBLE);
                binding.contactRC.setVisibility(View.GONE);
            } else {
                binding.noContact.setVisibility(View.GONE);
                binding.contactRC.setVisibility(View.VISIBLE);
            }
            Stash.put(Constants.REQUESTERS, usersList);
            ContactsAdapter adapter = new ContactsAdapter(requireContext(), usersList, (userID, position) -> {
                startActivity(new Intent(requireContext(), UserProfileActivity.class)
                        .putExtra("USER_ID", userID)
                        .putExtra("REQUESTER_ID", "")
                        .putExtra("REQUEST_ID", "")
                        .putExtra("PREVIEW", true)
                );
            });
            binding.contactRC.setAdapter(adapter);
        });

        binding.attachCamera.setOnClickListener(v -> {
            attachCamera();
        });

        binding.attachImage.setOnClickListener(v -> {
            attachImage();
        });

        binding.attachDocument.setOnClickListener(v -> {
            attachDocument();
        });

        binding.pause.setOnClickListener(v -> {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
        });
        binding.stop.setOnClickListener(v -> {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
        });

        binding.mic.listen.setOnClickListener(v -> {
            if (listeningAnimation == null || !listeningAnimation.isRunning()) {
                listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                speechRecognitionManager.startListening();
            } else {
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
                listeningAnimation = null;
                speechRecognitionManager.stopListening();
            }
        });

        return binding.getRoot();
    }

    private void attachCamera() {
        ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .maxResultSize(1080, 1080).createIntent(intent -> {
                    pickImageLauncher.launch(intent);
                    return null;
                });
    }

    private void attachImage() {
        ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(1024)
                .maxResultSize(1080, 1080).createIntent(intent -> {
                    pickImageLauncher.launch(intent);
                    return null;
                });
    }

    private void attachDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        pickDocumentLauncher.launch(intent);
    }

    DocumentsAdapter documentsAdapter;
    private void updateView() {
        if (list.isEmpty()) {
            binding.documentsRC.setVisibility(View.GONE);
            binding.noDocument.setVisibility(View.VISIBLE);
        } else {
            binding.documentsRC.setVisibility(View.VISIBLE);
            binding.noDocument.setVisibility(View.GONE);
        }

        documentsAdapter = new DocumentsAdapter(requireContext(), list, pos -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Attachment")
                    .setMessage("Are you sure you want to remove this attachment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        list.remove(pos);
                        documentsAdapter.notifyItemRemoved(pos);
                        Stash.put(Constants.DOCUMENTS, list);
                        if (list.isEmpty()) {
                            binding.documentsRC.setVisibility(View.GONE);
                            binding.noDocument.setVisibility(View.VISIBLE);
                        } else {
                            binding.documentsRC.setVisibility(View.VISIBLE);
                            binding.noDocument.setVisibility(View.GONE);
                        }
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        binding.documentsRC.setAdapter(documentsAdapter);
        Stash.put(Constants.DOCUMENTS, list);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Speech.init(requireContext(), requireContext().getPackageName());
                        speechRecognitionManager = new SpeechRecognitionManager(requireContext(), speechUtils);
                        listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                        speechRecognitionManager.startListening();
                    });
                }
            }, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancel the animation when the activity is paused
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        if (speechRecognitionManager != null) {
            speechRecognitionManager.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel the animation when the activity is destroyed
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        if (speechRecognitionManager != null) {
            speechRecognitionManager.destroy();
        }
    }

    // Helper method to get file size from Uri
    private long getFileSize(Uri uri) throws IOException {
        long fileSize = 0;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex);
                    }
                }
            }
        }

        // Fallback to InputStream if size is still 0
        if (fileSize == 0) {
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    fileSize = inputStream.available();
                }
            }
        }
        return fileSize;
    }


    // Helper method to show a Toast message
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}