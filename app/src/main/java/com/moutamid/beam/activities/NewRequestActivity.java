package com.moutamid.beam.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.ActivityNewRequestBinding;
import com.moutamid.beam.fragments.CategoryFragment;
import com.moutamid.beam.fragments.DescriptionFragment;
import com.moutamid.beam.fragments.DocumentsFragment;
import com.moutamid.beam.fragments.PreviewRequestFragment;
import com.moutamid.beam.models.DocumentLinkModel;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.notification.FCMNotificationHelper;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.FileUtils;
import com.moutamid.beam.utilis.Stash;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NewRequestActivity extends AppCompatActivity {
    private static final String TAG = "NewRequestActivity";
    ActivityNewRequestBinding binding;
    RequestModel newRequest;
    ArrayList<DocumentModel> list;
    ArrayList<DocumentLinkModel> documents;
    ProgressDialog progressDialog;
    UserModel userModel;
    AtomicInteger current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CategoryFragment()).commit();
        current = new AtomicInteger();
        binding.previous.setVisibility(View.GONE);

        userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);

        binding.next.setOnClickListener(v -> {
            binding.previous.setVisibility(View.VISIBLE);
            if (current.get() == 0) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DescriptionFragment()).commit();
                current.incrementAndGet();
            } else if (current.get() == 1) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DocumentsFragment()).commit();
                current.incrementAndGet();
            } else if (current.get() == 2) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new PreviewRequestFragment()).commit();
                current.incrementAndGet();
                binding.next.setText("Publish");
            } else {
                send();
            }
        });

        binding.previous.setOnClickListener(v -> {
            if (current.get() == 1) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CategoryFragment()).commit();
                current.decrementAndGet();
                binding.previous.setVisibility(View.GONE);
            } else if (current.get() == 2) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DescriptionFragment()).commit();
                current.decrementAndGet();
            } else if (current.get() == 3) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DocumentsFragment()).commit();
                current.decrementAndGet();
            }
            binding.next.setText("Next");
        });

        documents = new ArrayList<>();


        binding.toolbar.back.setOnClickListener(v -> {
            Stash.clear(Constants.SAVE_REQUEST);
            Stash.clear(Constants.DOCUMENTS);
            Stash.clear(Constants.REQUESTERS);
            getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbar.refresh.setVisibility(View.VISIBLE);
        binding.toolbar.title.setText("New Request");

        binding.toolbar.refresh.setOnClickListener(v -> {
            refresh();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Document ... ");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
    }


    private void send() {
        list = Stash.getArrayList(Constants.DOCUMENTS, DocumentModel.class);
        newRequest = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);
        if (valid()) {
            if (list.isEmpty()) {
                uploadModel();
            } else {
                uploadDocuments(0);
            }
        }
    }

    private void refresh() {
        Stash.clear(Constants.SAVE_REQUEST);
        Stash.clear(Constants.DOCUMENTS);
        Stash.clear(Constants.REQUESTERS);
        Intent intent = getIntent();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    private boolean valid() {
        Log.d(TAG, "valid: ");
        newRequest = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);
        if (newRequest != null) {
            if (newRequest.description == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DescriptionFragment()).commit();
                current.set(1);
                binding.next.setText("Next");
                Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (newRequest.description.isEmpty()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new DescriptionFragment()).commit();
                current.set(1);
                binding.next.setText("Next");
                Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            refresh();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
    }

    private void uploadDocuments(int i) {
        progressDialog.show();
        double progressPerDocument = 100.0 / list.size();
        if (i != list.size()) {
            DocumentModel document = list.get(i);
            String fileName = FileUtils.getFileName(this, Uri.parse(document.uri));
            Constants.storageReference(Constants.auth().getCurrentUser().getUid()).child(fileName).putFile(Uri.parse(document.uri))
                    .addOnSuccessListener(taskSnapshot -> {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            documents.add(new DocumentLinkModel(uri.toString(), fileName, document.isDoc));
                            uploadDocuments(i + 1);
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double uploadedBytes = snapshot.getBytesTransferred();
                            double overallProgress = (i * progressPerDocument) + ((uploadedBytes / snapshot.getTotalByteCount()) * progressPerDocument);
                            progressDialog.setProgress((int) overallProgress);
                        }
                    });
        } else {
            progressDialog.dismiss();
            uploadModel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Stash.clear(Constants.SAVE_REQUEST);
        Stash.clear(Constants.DOCUMENTS);
        Stash.clear(Constants.REQUESTERS);
    }

    private void uploadModel() {
        Constants.showDialog();
        newRequest.documents = new ArrayList<>(documents);
        Log.d(TAG, "uploadModel: documents " + newRequest.documents.size());
        newRequest.ID = UUID.randomUUID().toString();
        newRequest.timestamp = new Date().getTime();
        newRequest.userID = userModel.id;
        Constants.databaseReference().child(Constants.REQUESTS).child(userModel.id).child(newRequest.ID).setValue(newRequest)
                .addOnSuccessListener(unused -> {
                    sendNotifications();
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotifications() {
        ArrayList<UserModel> list = Stash.getArrayList(Constants.REQUESTERS, UserModel.class);
        for (UserModel user : list) {
            new FCMNotificationHelper(this).sendNotification(user.id, user.name, newRequest.description, newRequest.ID, newRequest.userID, "null");
        }
        Constants.dismissDialog();
        Toast.makeText(this, "Request Added", Toast.LENGTH_SHORT).show();
        getOnBackPressedDispatcher().onBackPressed();
    }

}