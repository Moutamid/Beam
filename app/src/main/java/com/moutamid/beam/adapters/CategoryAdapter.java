package com.moutamid.beam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.beam.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryVH> {

    Context context;
    ArrayList<String> list;

    public CategoryAdapter(Context context, ArrayList<String> list) {
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
        TextView text;
        public CategoryVH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

}
