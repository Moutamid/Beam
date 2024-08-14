package com.moutamid.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.transition.Hold;
import com.moutamid.beam.R;
import com.moutamid.beam.activities.RequestPreviewActivity;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestVH> {

    Context context;
    ArrayList<RequestModel> list;

    public RequestsAdapter(Context context, ArrayList<RequestModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RequestVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestVH holder, int position) {
        RequestModel requestModel = list.get(holder.getAbsoluteAdapterPosition());
        holder.title.setText(requestModel.title);
        holder.name.setText(requestModel.username);
        holder.description.setText(requestModel.description);
        Glide.with(context).load(requestModel.userImage).placeholder(R.drawable.profile_icon).into(holder.image);
        holder.postTime.setText("Posted " + Constants.getTime(requestModel.timestamp));

        holder.deadline.setText("Deadline : " +  new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(requestModel.deadline));

        holder.itemView.setOnClickListener(v -> context.startActivity(new Intent(context, RequestPreviewActivity.class)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RequestVH extends RecyclerView.ViewHolder{
        ImageView image;
        TextView title, name, postTime, deadline, description;
        public RequestVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            postTime = itemView.findViewById(R.id.postTime);
            deadline = itemView.findViewById(R.id.deadline);
            description = itemView.findViewById(R.id.description);
        }
    }

}
