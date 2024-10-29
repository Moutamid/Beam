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
import com.moutamid.beam.models.CategoryModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryVH> {
    Context context;
    ArrayList<CategoryModel> list;
    CategoryListener categoryListener;
    private int selectedItemPosition = 0;
    boolean isVertical;
    RequestModel requestModel;

    public CategoryAdapter(Context context, ArrayList<CategoryModel> list, CategoryListener categoryListener, boolean isVertical) {
        this.context = context;
        this.list = list;
        this.categoryListener = categoryListener;
        this.isVertical = isVertical;
    }

    @NonNull
    @Override
    public CategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isVertical) {
            return new CategoryVH(LayoutInflater.from(context).inflate(R.layout.category_vertical, parent, false));
        }
        return new CategoryVH(LayoutInflater.from(context).inflate(R.layout.category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryVH holder, int position) {
        CategoryModel s = list.get(holder.getAbsoluteAdapterPosition());
        holder.text.setText(s.name);

        if (isVertical) {
            requestModel = (RequestModel) Stash.getObject(Constants.SAVE_REQUEST, RequestModel.class);
            if (requestModel != null) {
                if (requestModel.category.equals(s.name)) {
                    categoryListener.selected(s.name);

                    int previousItemPosition = selectedItemPosition;
                    selectedItemPosition = holder.getAbsoluteAdapterPosition();

                    holder.itemView.post(() -> {
                        notifyItemChanged(previousItemPosition);
                        notifyItemChanged(selectedItemPosition);
                    });
                }
            } else if (holder.getAbsoluteAdapterPosition() == 0) {
                categoryListener.selected(s.name);
            }
        }

        if (categoryListener != null) {
            if (holder.getAbsoluteAdapterPosition() == selectedItemPosition) {
                holder.text.setTextColor(context.getColor(R.color.green));
                holder.text.setStrokeColor(ColorStateList.valueOf(context.getColor(R.color.green)));
                holder.text.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green_light_2)));
            } else {
                holder.text.setTextColor(context.getColor(R.color.black));
                holder.text.setStrokeColor(ColorStateList.valueOf(context.getColor(R.color.grey)));
                holder.text.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            }

            holder.itemView.setOnClickListener(v -> {
                categoryListener.selected(s.name);

                holder.itemView.post(() -> {
                    int previousItemPosition = selectedItemPosition;
                    selectedItemPosition = holder.getAbsoluteAdapterPosition();

                    notifyItemChanged(previousItemPosition);
                    notifyItemChanged(selectedItemPosition);
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoryVH extends RecyclerView.ViewHolder {
        MaterialButton text;

        public CategoryVH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

}
