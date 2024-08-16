package com.moutamid.beam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moutamid.beam.R;
import com.moutamid.beam.models.MessageModel;
import com.moutamid.beam.utilis.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> {
    Context context;
    ArrayList<MessageModel> list;
    public static final int CHAT_LEFT = 1;
    public static final int CHAT_RIGHT = 2;

    public ChatAdapter(Context context, ArrayList<MessageModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel model = list.get(position);
        if (model.senderID.equals(Constants.auth().getCurrentUser().getUid())) {
            return CHAT_RIGHT;
        } else {
            return CHAT_LEFT;
        }
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CHAT_LEFT) {
            return new ChatVH(LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false));
        } else {
            return new ChatVH(LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH holder, int position) {
        MessageModel model = list.get(holder.getAdapterPosition());
        holder.message.setText(model.message);
        holder.date.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(model.timestamp));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChatVH extends RecyclerView.ViewHolder {
        TextView message, date;

        public ChatVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            date = itemView.findViewById(R.id.date);
        }
    }

}
