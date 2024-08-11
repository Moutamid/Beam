package com.moutamid.beam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.beam.R;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentVh> {

    @NonNull
    @Override
    public DocumentVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentVh(LayoutInflater.from(parent.getContext()).inflate(R.layout.documents, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentVh holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class DocumentVh extends RecyclerView.ViewHolder{
        public DocumentVh(@NonNull View itemView) {
            super(itemView);
        }
    }

}
