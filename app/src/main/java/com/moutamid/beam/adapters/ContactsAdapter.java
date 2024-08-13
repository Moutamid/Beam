package com.moutamid.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.beam.R;
import com.moutamid.beam.activities.UserProfileActivity;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactVH> {

    Context context;

    public ContactsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ContactVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactVH holder, int position) {
        holder.itemView.setOnClickListener(v -> context.startActivity(new Intent(context, UserProfileActivity.class)));
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public static class ContactVH extends RecyclerView.ViewHolder {
        public ContactVH(@NonNull View itemView) {
            super(itemView);
        }
    }

}
