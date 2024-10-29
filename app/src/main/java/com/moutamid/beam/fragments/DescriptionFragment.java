package com.moutamid.beam.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.moutamid.beam.R;
import com.moutamid.beam.adapters.DocumentsAdapter;
import com.moutamid.beam.adapters.MandatoryAdapter;
import com.moutamid.beam.databinding.FragmentDescriptionBinding;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DescriptionFragment extends Fragment {
    FragmentDescriptionBinding binding;
    RequestModel requestModel;

    public DescriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDescriptionBinding.inflate(getLayoutInflater(), container, false);

        requestModel = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);

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

        return binding.getRoot();
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