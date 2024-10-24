package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.adapters.ContactsAdapter;
import com.moutamid.beam.adapters.DocumentsAdapter;
import com.moutamid.beam.databinding.ActivityNewRequestBinding;
import com.moutamid.beam.models.DocumentLinkModel;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.FileUtils;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;

import net.gotev.speech.Speech;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NewRequestActivity extends AppCompatActivity {
    private static final String TAG = "NewRequestActivity";
    private static final int PICK_FROM_CAMERA = 1001;
    private static final int PICK_FROM_GALLERY = 1002;
    private static final int PICK_DOCUMENT = 1003;
    ActivityNewRequestBinding binding;
    RequestModel newRequest;
    ArrayList<DocumentModel> list;
    ArrayList<DocumentLinkModel> documents;
    final Calendar calendar = Calendar.getInstance();
    ProgressDialog progressDialog;
    UserModel userModel;


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
            if (result.toLowerCase(Locale.ROOT).contains("go back")) {
                getOnBackPressedDispatcher().onBackPressed();
            } else if (result.toLowerCase(Locale.ROOT).contains("refresh")) {
                refresh();
            } else if (result.toLowerCase(Locale.ROOT).contains("attach document")) {
                attachDocument();
            } else if (result.toLowerCase(Locale.ROOT).contains("attach image") || result.toLowerCase(Locale.ROOT).contains("open gallery")) {
                attachImage();
            }  else if (result.toLowerCase(Locale.ROOT).contains("attach image from camera") || result.toLowerCase(Locale.ROOT).contains("open camera")) {
                attachCamera();
            }  else if (result.toLowerCase(Locale.ROOT).contains("select deadline") || result.toLowerCase(Locale.ROOT).contains("open deadline")) {
                selectDeadline();
            } else if (result.toLowerCase(Locale.ROOT).contains("create request") || result.toLowerCase(Locale.ROOT).contains("send request")) {
                send();
            } else {
                if (binding.name.getEditText().hasFocus()) {
                    binding.name.getEditText().setText(result.replace(" ", "").replace("-", ""));
                }
                if (binding.description.hasFocus()) {
                    binding.description.setText(result.replace(" ", "").replace("-", ""));
                }
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(NewRequestActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documents = new ArrayList<>();
        list = new ArrayList<>();

        newRequest = new RequestModel();

        userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        Glide.with(this).load(userModel.image).placeholder(R.drawable.profile_icon).into(binding.image);

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.refresh.setVisibility(View.VISIBLE);
        binding.toolbar.title.setText("New Request");

        binding.toolbar.refresh.setOnClickListener(v -> {
            refresh();
        });

        ArrayList<UserModel> usersList = new ArrayList<>();

        String[] service_categories = getResources().getStringArray(R.array.service_categories);
        ArrayAdapter<String> subject = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, service_categories);
        binding.categoryList.setAdapter(subject);

        binding.categoryList.setOnItemClickListener((parent, view, position, id) -> {
            String category = service_categories[position];
            Log.d(TAG, "onCreate: " + category);

            Constants.databaseReference().child(Constants.USER).get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    usersList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (Constants.auth().getCurrentUser() != null) {
                            if (user != null) {
                                if (!user.id.equals(Constants.auth().getCurrentUser().getUid()) && user.category.equals(category)) {
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
                ContactsAdapter adapter = new ContactsAdapter(NewRequestActivity.this, usersList,
                        userID -> startActivity(new Intent(this, UserProfileActivity.class).putExtra("USER_ID", userID))
                );
                binding.contactRC.setAdapter(adapter);
            });

        });

        binding.calender.setOnClickListener(v -> {
            selectDeadline();
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Document ... ");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);

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

        binding.mandatory.setOnClickListener(v -> {
            addMandatory();
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

        binding.send.setOnClickListener(v -> {
            send();
        });
    }

    private void selectDeadline() {
        DatePickerDialog.OnDateSetListener date = (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            newRequest.deadline = calendar.getTime().getTime();
            binding.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        };

        new DatePickerDialog(this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void attachCamera() {
        ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(PICK_FROM_CAMERA);
    }

    private void attachImage() {
        ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(PICK_FROM_GALLERY);
    }

    private void attachDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_DOCUMENT);
    }

    private void send() {
        if (valid()) {
            if (list.isEmpty()) {
                uploadModel();
            } else {
                uploadDocuments(0);
            }
        }
    }

    private void refresh() {
        binding.name.getEditText().setText("");
        binding.category.getEditText().setText("");
        binding.description.setText("");
        Intent intent = getIntent();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    private boolean valid() {
        if (binding.category.getEditText().getText().toString().isEmpty()) {
            binding.category.getEditText().setError("required*");
            binding.category.getEditText().requestFocus();
            return false;
        }
        if (binding.name.getEditText().getText().toString().isEmpty()) {
            binding.name.getEditText().setError("required*");
            binding.name.getEditText().requestFocus();
            return false;
        }
        if (binding.description.getText().toString().isEmpty()) {
            binding.description.setError("required*");
            binding.description.requestFocus();
            return false;
        }
        if (newRequest.deadline == 0) {
            Toast.makeText(this, "Deadline is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    Speech.init(this, getPackageName());
                    speechRecognitionManager = new SpeechRecognitionManager(this, speechUtils);
                    listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                    speechRecognitionManager.startListening();
                });
            }, 1000);
        }
    }

    private void uploadDocuments(int i) {
        progressDialog.show();
        double progressPerDocument = 100.0 / list.size();
        if (i != list.size()) {
            DocumentModel document = list.get(i);
            String fileName = FileUtils.getFileName(this, document.uri);
            Constants.storageReference(Constants.auth().getCurrentUser().getUid()).child(fileName).putFile(document.uri)
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

    private void uploadModel() {
        Constants.showDialog();
        newRequest.documents = new ArrayList<>(documents);
        newRequest.ID = UUID.randomUUID().toString();
        newRequest.title = binding.name.getEditText().getText().toString();
        newRequest.description = binding.description.getText().toString();
        newRequest.category = binding.category.getEditText().getText().toString();
        newRequest.timestamp = new Date().getTime();
        newRequest.userID = userModel.id;

        Constants.databaseReference().child(Constants.REQUESTS).child(userModel.id).child(newRequest.ID).setValue(newRequest)
                .addOnSuccessListener(unused -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, "Request Added", Toast.LENGTH_SHORT).show();
                    getOnBackPressedDispatcher().onBackPressed();
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addMandatory() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_mandatory);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        MaterialButton addRow = dialog.findViewById(R.id.addRow);
        MaterialButton exit = dialog.findViewById(R.id.exit);
        MaterialButton confirm = dialog.findViewById(R.id.confirm);
        LinearLayout mandatoryLayout = dialog.findViewById(R.id.mandatoryLayout);

        LayoutInflater inflater = getLayoutInflater();
        View customEditTextLayout = inflater.inflate(R.layout.row_edittext, null);
        mandatoryLayout.addView(customEditTextLayout);

        addRow.setOnClickListener(v -> {
            LayoutInflater inflater1 = getLayoutInflater();
            View customEditTextLayout1 = inflater1.inflate(R.layout.row_edittext, null);
            mandatoryLayout.addView(customEditTextLayout1);
        });

        exit.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < mandatoryLayout.getChildCount(); i++) {
                View view = mandatoryLayout.getChildAt(i);
                if (view instanceof RelativeLayout) {
                    RelativeLayout textInputLayout = (RelativeLayout) view;
                    TextInputLayout customEditText = textInputLayout.findViewById(R.id.addColumn);

                    String enteredText = customEditText.getEditText().getText().toString();
                    if (!enteredText.isEmpty())
                        list.add(enteredText);
                }
            }
            if (newRequest.mandatory == null) newRequest.mandatory = new ArrayList<>();
            newRequest.mandatory.addAll(list);
            dialog.dismiss();
            updateView();
        });
    }

    DocumentsAdapter documentsAdapter;

    private void updateView() {
        if (newRequest.mandatory != null) {
            CategoryAdapter adapter = new CategoryAdapter(this, newRequest.mandatory, null);
            binding.mandatoryRC.setAdapter(adapter);
        }

        if (list.isEmpty()) {
            binding.documentsRC.setVisibility(View.GONE);
            binding.noDocument.setVisibility(View.VISIBLE);
        } else {
            binding.documentsRC.setVisibility(View.VISIBLE);
            binding.noDocument.setVisibility(View.GONE);
        }

        documentsAdapter = new DocumentsAdapter(this, list, pos -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Remove Attachment")
                    .setMessage("Are you sure you want to remove this attachment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        list.remove(pos);
                        documentsAdapter.notifyItemRemoved(pos);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cancel the animation when the activity is paused
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        speechRecognitionManager.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the animation when the activity is destroyed
        if (listeningAnimation != null) {
            listeningAnimation.cancel();
        }
        speechRecognitionManager.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DOCUMENT) {
            if (resultCode == RESULT_OK && data != null) {
                list.add(new DocumentModel(data.getData(), true));
                updateView();
            }
        } else {
            if (resultCode == RESULT_OK && data != null) {
                list.add(new DocumentModel(data.getData(), false));
                updateView();
            }
        }
    }
}