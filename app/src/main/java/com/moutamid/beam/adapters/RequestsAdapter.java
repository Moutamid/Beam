package com.moutamid.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.Hold;
import com.moutamid.beam.R;
import com.moutamid.beam.activities.RequestPreviewActivity;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestVH> {

    Context context;

    public RequestsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RequestVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestVH holder, int position) {
        holder.itemView.setOnClickListener(v -> context.startActivity(new Intent(context, RequestPreviewActivity.class)));
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public static class RequestVH extends RecyclerView.ViewHolder{

        public RequestVH(@NonNull View itemView) {
            super(itemView);
        }
    }

}
