package com.moutamid.beam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.beam.R;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestVH> {

    @NonNull
    @Override
    public RequestVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class RequestVH extends RecyclerView.ViewHolder{

        public RequestVH(@NonNull View itemView) {
            super(itemView);
        }
    }

}
