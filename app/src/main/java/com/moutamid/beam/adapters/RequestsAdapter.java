package com.moutamid.beam.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.beam.R;
import com.moutamid.beam.activities.RequestPreviewActivity;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestVH> implements Filterable {
    Context context;
    ArrayList<RequestModel> list;
    ArrayList<RequestModel> listAll;

    public RequestsAdapter(Context context, ArrayList<RequestModel> list) {
        this.context = context;
        this.list = list;
        this.listAll = new ArrayList<>(list);
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
        holder.description.setText(requestModel.description);
        holder.postTime.setText("Posted " + Constants.getTime(requestModel.timestamp));
        holder.deadline.setText("Deadline : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(requestModel.deadline));

        Constants.databaseReference().child(Constants.USER).child(requestModel.userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        holder.name.setText(userModel.name);
                        Glide.with(context).load(userModel.image).placeholder(R.drawable.profile_icon).into(holder.image);

                        if (userModel.status) {
                            holder.status.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                        } else {
                            holder.status.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.stroke)));
                        }

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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.containDocument.setVisibility(View.GONE);
        holder.containImage.setVisibility(View.GONE);

        if (requestModel.documents != null) {
            if (!requestModel.documents.isEmpty()) {
                boolean hasDoc = requestModel.documents.stream().anyMatch(doc -> doc.isDoc);
                boolean hasNonDoc = requestModel.documents.stream().anyMatch(doc -> !doc.isDoc);
                if (hasDoc) holder.containDocument.setVisibility(View.VISIBLE);
                if (hasNonDoc) holder.containImage.setVisibility(View.VISIBLE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Stash.put(Constants.PASS_REQUEST, requestModel);
            context.startActivity(new Intent(context, RequestPreviewActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<RequestModel> filterList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filterList.addAll(listAll);
            } else {
                for (RequestModel listModel : listAll) {
                    if (listModel.title.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            listModel.category.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filterList.add(listModel);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends RequestModel>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class RequestVH extends RecyclerView.ViewHolder {
        ImageView image, containImage, containDocument;
        TextView title, name, postTime, deadline, description, rating;
        View status;

        public RequestVH(@NonNull View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.status);
            image = itemView.findViewById(R.id.image);
            containImage = itemView.findViewById(R.id.containImage);
            containDocument = itemView.findViewById(R.id.containDocument);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            postTime = itemView.findViewById(R.id.postTime);
            deadline = itemView.findViewById(R.id.deadline);
            description = itemView.findViewById(R.id.description);
            rating = itemView.findViewById(R.id.rating);
        }
    }

}
