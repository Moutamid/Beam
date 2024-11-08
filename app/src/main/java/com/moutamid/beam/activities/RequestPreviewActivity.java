package com.moutamid.beam.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.ContactsAdapter;
import com.moutamid.beam.adapters.DocumentsLinkAdapter;
import com.moutamid.beam.adapters.DocumentsList;
import com.moutamid.beam.adapters.ImageAdapter;
import com.moutamid.beam.adapters.MandatoryAdapter;
import com.moutamid.beam.databinding.ActivityRequestPreviewBinding;
import com.moutamid.beam.models.DocumentLinkModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RequestPreviewActivity extends AppCompatActivity {
    private static final String TAG = "RequestPreviewActivity";
    ActivityRequestPreviewBinding binding;
    RequestModel requestModel;
    ArrayList<RequestModel> requestList;
    ArrayList<UserModel> usersList;

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
            } else if (result.toLowerCase(Locale.ROOT).contains("reply")) {
                startActivity(new Intent(RequestPreviewActivity.this, RequestResponseActivity.class));
            } else if (result.toLowerCase(Locale.ROOT).contains("order")) {
                if (!passID.isEmpty()) {
                    startActivity(new Intent(RequestPreviewActivity.this, UserProfileActivity.class)
                            .putExtra("REQUESTER_ID", REQUESTER_ID)
                            .putExtra("REQUEST_ID", requestModel.ID)
                            .putExtra("USER_ID", passID));
                }
            }
        }

        @Override
        public void onError(String error) {
            if (listeningAnimation != null) {
                listeningAnimation.cancel();
                MicAnimation.cancelListeningAnimation(listeningAnimation, binding.mic.foreground, binding.mic.background);
            }
            Toast.makeText(RequestPreviewActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestModel = (RequestModel) Stash.getObject(Constants.PASS_REQUEST, RequestModel.class);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String itemId = data.getLastPathSegment();
            Log.d(TAG, "onCreate: itemId " + itemId);
        }

        requestList = new ArrayList<>();
        usersList = new ArrayList<>();

        binding.toolbar.title.setText("Preview");

        if (requestModel.userID.equals(Constants.auth().getCurrentUser().getUid())) {
            binding.toolbar.stop.setVisibility(View.VISIBLE);
        }

        binding.toolbar.share.setVisibility(View.VISIBLE);
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.toolbar.share.setOnClickListener(v -> {

            String message = "Title: " + requestModel.title + "\n\n" +
                    "Description: " + requestModel.description +
                    "If you're interested, please contact me for further discussion.\n\n" +
                    "Play Store Link: https://play.google.com/store/apps/details?id=" + getPackageName();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        binding.toolbar.stop.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Stop Request")
                    .setMessage("Are you sure you want to delete this request?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        Constants.databaseReference().child(Constants.REQUESTS).child(requestModel.userID).child(requestModel.ID).removeValue()
                                .addOnSuccessListener(unused -> {
                                    Constants.databaseReference().child(Constants.REQUESTS_REPLY).child(requestModel.ID).removeValue()
                                            .addOnSuccessListener(unused2 -> getOnBackPressedDispatcher().onBackPressed())
                                            .addOnFailureListener(e -> Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

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
                            String name = userModel.isAnonymous ? "Anonymous" : userModel.name;
                            binding.username.setText(name);
                            String image = userModel.isAnonymous ? "" : userModel.image;
                            Glide.with(RequestPreviewActivity.this).load(image).placeholder(R.drawable.profile_icon).into(binding.image);

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

        binding.containImage.setOnClickListener(v -> {
            imagePreview();
        });

        binding.containDocument.setOnClickListener(v -> {
            documentList();
        });

        if (requestModel.mandatory != null) {
            MandatoryAdapter categoryAdapter = new MandatoryAdapter(this, requestModel.mandatory);
            binding.madatoryItems.setAdapter(categoryAdapter);
        }

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

    }

    private void documentList() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.current_programs);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        RecyclerView recyclerView = dialog.findViewById(R.id.currentPrograms);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        DocumentsList documentsList = new DocumentsList(this, requestModel.documents, (link, filename) -> {
            dialog.dismiss();
            openDocument(link, filename);
        });
        recyclerView.setAdapter(documentsList);
    }

    private void openDocument(String url, String filename) {
        try {
            String fileExtension = filename.substring(filename.lastIndexOf(".") + 1);
            String mimeType = getMimeType(fileExtension);
            if (mimeType != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), mimeType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(Intent.createChooser(intent, "Open with"));
            } else {
                Toast.makeText(this, "Something went wrong with the file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "txt":
                return "text/plain";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "mkv":
                return "video/x-matroska";
            default:
                return null;
        }
    }

    private void imagePreview() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.imageviewer);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(true);
        dialog.show();

        RecyclerView recyclerView = dialog.findViewById(R.id.imageRC);
        MaterialCardView back = dialog.findViewById(R.id.back);
        TextView name = dialog.findViewById(R.id.name);

        name.setText(requestModel.title);
        back.setOnClickListener(v -> dialog.dismiss());

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(false);

        ArrayList<String> imageList = new ArrayList<>();

        for (DocumentLinkModel list : requestModel.documents) {
            if (!list.isDoc) {
                imageList.add(list.link);
            }
        }

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        ImageAdapter adapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        Constants.showDialog();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    Speech.init(this, getPackageName());
                    speechRecognitionManager = new SpeechRecognitionManager(this, speechUtils);
                    listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);
                    speechRecognitionManager.startListening();
                });
            }, 2000);
        }
        getReplies();
    }

    private void getReplies() {
        Constants.databaseReference().child(Constants.REQUESTS_REPLY).child(requestModel.ID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isDestroyed()) {
                            if (snapshot.exists()) {
                                requestList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    RequestModel model = dataSnapshot.getValue(RequestModel.class);
                                    requestList.add(model);
                                }
                            }
                            if (requestList.isEmpty()) {
                                Constants.dismissDialog();
                            } else {
                                usersList.clear();
                                getUsers(0);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();
                        Toast.makeText(RequestPreviewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUsers(int pos) {
        if (pos != requestList.size()) {
            RequestModel model = requestList.get(pos);
            Log.d(TAG, "getUsers: " + model.userID);
            Constants.databaseReference().child(Constants.USER).child(model.userID).get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
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
                getUsers(pos + 1);
            });
        } else {
            Constants.dismissDialog();
            if (usersList.isEmpty()) {
                binding.noContact.setVisibility(View.VISIBLE);
                binding.contactRC.setVisibility(View.GONE);
            } else {
                binding.noContact.setVisibility(View.GONE);
                binding.contactRC.setVisibility(View.VISIBLE);
            }
            ContactsAdapter adapter = new ContactsAdapter(RequestPreviewActivity.this, usersList, (userID, position) -> {
                showData(userID, position);
            });
            binding.contactRC.setAdapter(adapter);
        }
    }

    public String passID = "";
    public String REQUESTER_ID = "";

    private void showData(String userID, int position) {
        binding.replyLayout.setVisibility(View.VISIBLE);
//        RequestModel model = requestList.stream().filter(requestModel1 -> requestModel1.userID.equals(userID)).findFirst().get();
        RequestModel model = requestList.get(position);

        binding.replyTitle.setText(model.title);
        binding.replyDescription.setText(model.description);

        passID = userID;
        REQUESTER_ID = model.ID;

        binding.order.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)
                .putExtra("USER_ID", userID)
                .putExtra("REQUESTER_ID", REQUESTER_ID)
                .putExtra("REQUEST_ID", requestModel.ID)
        ));

        if (model.documents != null) {
            if (model.documents.isEmpty()) {
                binding.documentsRC.setVisibility(View.GONE);
                binding.noDocument.setVisibility(View.VISIBLE);
            } else {
                binding.documentsRC.setVisibility(View.VISIBLE);
                binding.noDocument.setVisibility(View.GONE);
            }

            DocumentsLinkAdapter documentsAdapter = new DocumentsLinkAdapter(this, model.documents, this::openDocument);
            binding.documentsRC.setAdapter(documentsAdapter);
        }

        if (model.mandatory != null) {
            MandatoryAdapter categoryAdapter = new MandatoryAdapter(this, model.mandatory);
            binding.madatoryItemsReply.setAdapter(categoryAdapter);
        }

    }
}