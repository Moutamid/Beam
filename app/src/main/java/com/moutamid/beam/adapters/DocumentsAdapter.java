package com.moutamid.beam.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moutamid.beam.R;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.utilis.FileUtils;

import java.util.ArrayList;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentVh> {

    public interface ClickListener {
        void onClick(int pos);
    }

    Context context;
    ArrayList<DocumentModel> list;
    ClickListener clickListener;

    public DocumentsAdapter(Context context, ArrayList<DocumentModel> list, ClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public DocumentVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentVh(LayoutInflater.from(context).inflate(R.layout.documents, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentVh holder, int position) {
        DocumentModel model = list.get(holder.getAbsoluteAdapterPosition());
        if (model.isDoc) {
            holder.image.setVisibility(View.GONE);
            holder.name.setVisibility(View.VISIBLE);
            String fileName = FileUtils.getFileName(context, Uri.parse(model.uri));
            String fileExtension = FileUtils.getFileExtension(fileName);
            holder.name.setText(fileName);
        } else {
            holder.image.setVisibility(View.VISIBLE);
            holder.name.setVisibility(View.GONE);
            Glide.with(context).load(Uri.parse(model.uri)).placeholder(R.color.white).into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onClick(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DocumentVh extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name;
        public DocumentVh(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
        }
    }

}
