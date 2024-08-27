package com.moutamid.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.beam.utilis.Stash;
import com.google.android.material.button.MaterialButton;
import com.moutamid.beam.models.DocumentLinkModel;
import com.moutamid.beam.R;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DocumentsList extends RecyclerView.Adapter<DocumentsList.ProgramVh> {

    public interface ClickListener {
        void onClick(String link, String filename);
    }

    Context context;
    List<DocumentLinkModel> list;
    private static final String TAG = "CurrentPrograms";
    ClickListener listener;

    public DocumentsList(Context context, List<DocumentLinkModel> list, ClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProgramVh(LayoutInflater.from(context).inflate(R.layout.current_program_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramVh holder, int position) {
        DocumentLinkModel document = list.get(holder.getAbsoluteAdapterPosition());
        holder.button.setText(document.name);
        holder.itemView.setOnClickListener(v -> {
            listener.onClick(document.link, document.name);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ProgramVh extends RecyclerView.ViewHolder {
        MaterialButton button;
        public ProgramVh(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
        }
    }

}
