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
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;
import com.moutamid.beam.utilis.Stash;

import java.util.ArrayList;
import java.util.Locale;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactVH> {
    Context context;
    ArrayList<UserModel> list;
    UserModel currentUser;

    public interface ContactListener {
        void onClick(String userID, int pos);
    }

    ContactListener listener;

    public ContactsAdapter(Context context, ArrayList<UserModel> list, ContactListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        currentUser = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
    }

    @NonNull
    @Override
    public ContactVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactVH holder, int position) {
        UserModel userModel = list.get(holder.getAbsoluteAdapterPosition());
        String name = userModel.isAnonymous ? "Anonymous" : userModel.name;
        holder.name.setText(name);
        String image = userModel.isAnonymous ? "" : userModel.image;
        Glide.with(context).load(image).placeholder(R.drawable.profile_icon).into(holder.image);
        double distance = Constants.calculateDistance(currentUser.location.lat, currentUser.location.log, userModel.location.lat, userModel.location.log);
        holder.distance.setText(Constants.formatDistance(distance));

        if (userModel.rating != null) {
            float rating = 0;
            for (double commentModel : userModel.rating) rating += commentModel;
            float total = rating / userModel.rating.size();
            String rate = String.format(Locale.getDefault(), "%.2f", total) + " (" + userModel.rating.size() + ")";
            if (userModel.rating.size() > 1) holder.rating.setText(rate);
            else holder.rating.setText(userModel.rating.get(0) + " (1)");
        } else {
            holder.rating.setText("0.0 (0)");
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                listener.onClick(userModel.id, holder.getAbsoluteAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ContactVH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView distance, name, rating;

        public ContactVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            distance = itemView.findViewById(R.id.distance);
            name = itemView.findViewById(R.id.name);
            rating = itemView.findViewById(R.id.rating);
        }
    }

}
