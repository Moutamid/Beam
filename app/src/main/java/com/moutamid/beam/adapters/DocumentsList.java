package com.moutamid.beam.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.moutamid.beam.R;
import com.moutamid.beam.models.DocumentLinkModel;

import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.List;

public class DocumentsList extends RecyclerView.Adapter<DocumentsList.ProgramVh> {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(DocumentsList.class);

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

        holder.download.setOnClickListener(v -> {
            startDownload(document.link);
        });

        holder.itemView.setOnClickListener(v -> {
            listener.onClick(document.link, document.name);
        });
    }

    private void startDownload(String audio) {
        Log.d(TAG, "startDownload: " + audio);
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.download_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        TextView pro = dialog.findViewById(R.id.message);
        LinearProgressIndicator progressbar = dialog.findViewById(R.id.progressbar);

        StorageReference sr = FirebaseStorage.getInstance().getReferenceFromUrl(audio);
        Log.d(TAG, "getDownloadUrl: " + sr.getDownloadUrl());
        Log.d(TAG, "name: " + sr.getName());
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + context.getString(R.string.app_name) + File.separator;
        File f = new File(path);
        f.mkdirs();
        File local = new File(path, sr.getName());
        Log.d(TAG, "local: " + local.getAbsolutePath());
        sr.getFile(local)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressbar.setProgress((int) progress, true);
                    pro.setText((int) progress + "/100%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    dialog.dismiss();
                    Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ProgramVh extends RecyclerView.ViewHolder {
        TextView button;
        MaterialCardView download;

        public ProgramVh(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
            download = itemView.findViewById(R.id.download);
        }
    }

}
