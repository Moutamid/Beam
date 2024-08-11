package com.moutamid.beam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.beam.R;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactVH> {

    @NonNull
    @Override
    public ContactVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ContactVH extends RecyclerView.ViewHolder {
        public ContactVH(@NonNull View itemView) {
            super(itemView);
        }
    }

}
