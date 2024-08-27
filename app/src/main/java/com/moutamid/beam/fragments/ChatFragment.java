package com.moutamid.beam.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moutamid.beam.utilis.Stash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.moutamid.beam.adapters.ChatAdapter;
import com.moutamid.beam.databinding.FragmentChatBinding;
import com.moutamid.beam.models.MessageModel;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.notification.FcmNotificationsSender;
import com.moutamid.beam.utilis.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;


public class ChatFragment extends Fragment {
    FragmentChatBinding binding;
    ArrayList<MessageModel> list;
    ChatAdapter adapter;
    UserModel userModel;
    String chatID;
    UserModel stash;

    public ChatFragment() {
        // Required empty public constructor
    }

    public ChatFragment(UserModel userModel) {
        this.userModel = userModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(getLayoutInflater(), container, false);

        list = new ArrayList<>();

        stash = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);

        if (stash.id.compareTo(userModel.id) < 0) {
            chatID = stash.id + "_" + userModel.id;
        } else {
            chatID = userModel.id + "_" + stash.id;
        }

        binding.chat.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chat.setHasFixedSize(false);

        adapter = new ChatAdapter(requireContext(), list);
        binding.chat.setAdapter(adapter);

        Constants.databaseReference().child(Constants.MESSAGES).child(chatID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            MessageModel messageModel = snapshot.getValue(MessageModel.class);
                            list.add(messageModel);
                            list.sort(Comparator.comparingLong(o -> o.timestamp));
                            adapter.notifyItemInserted(list.size() - 1);
                            binding.chat.scrollToPosition(list.size() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {

                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.send.setOnClickListener(v -> {
            if (!binding.message.getText().toString().isEmpty()) {
                sendMessage();
            }
        });

        return binding.getRoot();
    }

    private void sendMessage() {
        String m = binding.message.getText().toString().trim();
        binding.message.setText("");
        MessageModel model = new MessageModel();
        model.id = UUID.randomUUID().toString();
        model.senderID = Constants.auth().getCurrentUser().getUid();
        model.chatID = chatID;
        model.timestamp = new Date().getTime();
        model.message = m;

        Constants.databaseReference().child(Constants.MESSAGES).child(chatID).child(model.id)
                .setValue(model).addOnSuccessListener(unused -> {
                    new FcmNotificationsSender("/topics/" + userModel.id, stash.name, m, requireContext(), requireActivity()).SendNotifications();
                });
    }
}