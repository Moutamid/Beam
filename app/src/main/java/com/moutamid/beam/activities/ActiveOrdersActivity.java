package com.moutamid.beam.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.beam.adapters.ActiveOrdersAdapter;
import com.moutamid.beam.databinding.ActivityActiveOrdersBinding;
import com.moutamid.beam.models.OrderModel;
import com.moutamid.beam.models.RequestModel;
import com.moutamid.beam.utilis.Constants;

import java.util.ArrayList;

public class ActiveOrdersActivity extends AppCompatActivity {
    ActivityActiveOrdersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActiveOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.title.setText("Active Orders");
        binding.toolbar.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setHasFixedSize(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        adapter = new ActiveOrdersAdapter(ActiveOrdersActivity.this, new ArrayList<>());
        binding.recycler.setAdapter(adapter);
        getData();
    }

    ArrayList<OrderModel> orders;
    ArrayList<RequestModel> list;
    ActiveOrdersAdapter adapter;

    private void getData() {
        Constants.showDialog();
        Constants.databaseReference().child(Constants.ORDER).child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orders = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);
                                orders.add(orderModel);
                            }
                        } else {
                            Constants.dismissDialog();
                            Toast.makeText(ActiveOrdersActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                        }
                        if (!orders.isEmpty()) {
                            list = new ArrayList<>();
                            getOrders(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();
                        Toast.makeText(ActiveOrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getOrders(int i) {
        OrderModel order = orders.get(i);
        Constants.databaseReference().child(Constants.REQUESTS_REPLY).child(order.requestID)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            RequestModel requestModel = dataSnapshot.getValue(RequestModel.class);
                            requestModel.key = order.requestID;
                            if (requestModel.userID.equals(order.userID)) list.add(requestModel);
                        }
                        if (i == orders.size() - 1) {
                            Constants.dismissDialog();
                            adapter = new ActiveOrdersAdapter(ActiveOrdersActivity.this, list);
                            binding.recycler.setAdapter(adapter);
                        } else getOrders(i + 1);
                    } else {
                        Constants.dismissDialog();
                    }
                });
    }
}