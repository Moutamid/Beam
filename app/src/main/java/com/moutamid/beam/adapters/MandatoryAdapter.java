package com.moutamid.beam.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.moutamid.beam.R;
import com.moutamid.beam.listeners.CategoryListener;

import java.util.ArrayList;

public class MandatoryAdapter extends RecyclerView.Adapter<MandatoryAdapter.CategoryVH> {
    Context context;
    ArrayList<String> list;

    public MandatoryAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryVH(LayoutInflater.from(context).inflate(R.layout.category, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryVH holder, int position) {
        String s = list.get(holder.getAbsoluteAdapterPosition());
        holder.text.setText(s);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoryVH extends RecyclerView.ViewHolder{
        MaterialButton text;
        public CategoryVH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

}
