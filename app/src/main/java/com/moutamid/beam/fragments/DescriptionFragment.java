package com.moutamid.beam.fragments;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.MandatoryAdapter;
import com.moutamid.beam.databinding.FragmentDescriptionBinding;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.MicAnimation;
import com.moutamid.beam.utilis.SpeechRecognitionManager;
import com.moutamid.beam.utilis.SpeechUtils;
import com.moutamid.beam.utilis.Stash;

import net.gotev.speech.Speech;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DescriptionFragment extends Fragment {
    FragmentDescriptionBinding binding;
    RequestModel requestModel;
    private static final String TAG = "DescriptionFragment";

    public DescriptionFragment() {
        // Required empty public constructor
    }

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
            if (result.toLowerCase(Locale.ROOT).contains("select deadline") || result.toLowerCase(Locale.ROOT).contains("open deadline")) {
                selectDeadline();
            } else {
                if (binding.description.hasFocus()) {
                    binding.description.append(result + " ");
                    binding.description.setSelection(binding.description.getText().length());
                }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDescriptionBinding.inflate(getLayoutInflater(), container, false);

        requestModel = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);

        Log.d(TAG, "onCreateView: " + requestModel.category);

        if (requestModel.description != null) {
            binding.description.setText(requestModel.description);
        }
        updateView();
        if (requestModel.deadline == 0) {
            long currentTime = System.currentTimeMillis();
            requestModel.deadline = currentTime + (48 * 60 * 60 * 1000);
            Stash.put(Constants.SAVE_REQUEST, requestModel);
        } else {
            binding.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(requestModel.deadline));
        }

        binding.description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    String[] title = s.toString().split(" ");
                    int wordCount = Math.min(title.length, 10);
                    StringBuilder firstTenWords = new StringBuilder();
                    for (int i = 0; i < wordCount; i++) {
                        firstTenWords.append(title[i]);
                        if (i < wordCount - 1) {
                            firstTenWords.append(" ");
                        }
                    }
                    requestModel.title = String.valueOf(firstTenWords);
                    requestModel.description = s.toString().trim();
                } else {
                    requestModel.title = "";
                    requestModel.description = "";
                }
                Stash.put(Constants.SAVE_REQUEST, requestModel);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.mandatory.setOnClickListener(v -> {
            addMandatory();
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

    final Calendar calendar = Calendar.getInstance();

    private void selectDeadline() {
        DatePickerDialog.OnDateSetListener date = (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            requestModel.deadline = calendar.getTime().getTime();
            binding.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
            Stash.put(Constants.SAVE_REQUEST, requestModel);
        };
        new DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addMandatory() {
        Dialog dialog = new Dialog(requireContext());
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
            if (requestModel.mandatory == null) requestModel.mandatory = new ArrayList<>();
            requestModel.mandatory.addAll(list);
            dialog.dismiss();
            updateView();
        });
    }

    private void updateView() {
        if (requestModel.mandatory != null) {
            MandatoryAdapter adapter = new MandatoryAdapter(requireContext(), requestModel.mandatory);
            binding.mandatoryRC.setAdapter(adapter);
            Stash.put(Constants.SAVE_REQUEST, requestModel);
        }
    }

}