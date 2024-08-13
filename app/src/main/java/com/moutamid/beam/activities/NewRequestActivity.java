package com.moutamid.beam.activities;

import android.animation.AnimatorSet;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.CategoryAdapter;
import com.moutamid.beam.adapters.ContactsAdapter;
import com.moutamid.beam.adapters.DocumentsAdapter;
import com.moutamid.beam.databinding.ActivityNewRequestBinding;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewRequestActivity extends AppCompatActivity {
    private static final String TAG = "NewRequestActivity";
    private static final int PICK_FROM_CAMERA = 1001;
    private static final int PICK_FROM_GALLERY = 1002;
    private static final int PICK_DOCUMENT = 1003;
    ActivityNewRequestBinding binding;
    AnimatorSet listeningAnimation;
    private SpeechRecognitionManager speechRecognitionManager;
    RequestModel newRequest;
    ArrayList<DocumentModel> list;
    final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNewRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        list = new ArrayList<>();

        newRequest = new RequestModel();

        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.refresh.setVisibility(View.VISIBLE);
        binding.toolbar.title.setText("New Request");

        ContactsAdapter adapter = new ContactsAdapter(this);
        binding.contactRC.setAdapter(adapter);

        DatePickerDialog.OnDateSetListener date = (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            binding.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        };

        binding.calender.setOnClickListener(v -> {
            new DatePickerDialog(this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        listeningAnimation = MicAnimation.startListeningAnimation(binding.mic.foreground, binding.mic.background);

        speechRecognitionManager = new SpeechRecognitionManager(this, new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                // Handle the speech recognition results
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    Log.d(TAG, "onResults: " + spokenText);
                }
            }

            @Override
            public void onError(int error) {
                // Handle the error
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        speechRecognitionManager.startListening();

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
            addMandotory();
        });

        binding.attachCamera.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .cameraOnly()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(PICK_FROM_CAMERA);
        });

        binding.attachImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .galleryOnly()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(PICK_FROM_GALLERY);
        });

        binding.attachDocument.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_DOCUMENT);
        });
    }

    private void addMandotory() {
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
//        TextInputLayout customEditText = customEditTextLayout.findViewById(R.id.addColumn);

        mandatoryLayout.addView(customEditTextLayout);

        addRow.setOnClickListener(v -> {
            LayoutInflater inflater1 = getLayoutInflater();
            View customEditTextLayout1 = inflater1.inflate(R.layout.row_edittext, null);
//        TextInputLayout customEditText = customEditTextLayout.findViewById(R.id.addColumn);
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
            if (newRequest.Mandatory == null) newRequest.Mandatory = new ArrayList<>();
            newRequest.Mandatory.addAll(list);
            dialog.dismiss();
            updateView();
        });

    }

    private void updateView() {
        if (newRequest.Mandatory != null) {
            CategoryAdapter adapter = new CategoryAdapter(this, newRequest.Mandatory);
            binding.mandatoryRC.setAdapter(adapter);
        }

        if (list.isEmpty()) {
            binding.documentsRC.setVisibility(View.GONE);
            binding.noDocument.setVisibility(View.VISIBLE);
        } else {
            binding.documentsRC.setVisibility(View.VISIBLE);
            binding.noDocument.setVisibility(View.GONE);
        }

        DocumentsAdapter documentsAdapter = new DocumentsAdapter(this, list);
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