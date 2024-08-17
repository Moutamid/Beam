package com.moutamid.beam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moutamid.beam.R;
import com.moutamid.beam.models.DocumentLinkModel;
import com.moutamid.beam.models.DocumentModel;
import com.moutamid.beam.utilis.FileUtils;

import java.util.ArrayList;

public class DocumentsLinkAdapter extends RecyclerView.Adapter<DocumentsLinkAdapter.DocumentVh> {

    public interface ClickListener {
        void onClick(String link, String filename);
    }

    Context context;
    ArrayList<DocumentLinkModel> list;
    ClickListener clickListener;

    public DocumentsLinkAdapter(Context context, ArrayList<DocumentLinkModel> list, ClickListener clickListener) {
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
        DocumentLinkModel model = list.get(holder.getAbsoluteAdapterPosition());
        if (model.isDoc) {
            holder.image.setVisibility(View.GONE);
            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(model.name);
        } else {
            holder.image.setVisibility(View.VISIBLE);
            holder.name.setVisibility(View.GONE);
            Glide.with(context).load(model.link).placeholder(R.color.white).into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onClick(model.link, model.name));
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
