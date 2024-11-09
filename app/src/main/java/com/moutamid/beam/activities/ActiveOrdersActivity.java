package com.moutamid.beam.activities;

import android.os.Bundle;
import android.util.Log;
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

import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

public class ActiveOrdersActivity extends AppCompatActivity {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(ActiveOrdersActivity.class);
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
                        if (!isDestroyed() && !isFinishing()) {
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
                                adapter = new ActiveOrdersAdapter(ActiveOrdersActivity.this, list);
                                binding.recycler.setAdapter(adapter);
                                Log.d(TAG, "orders.size: " + orders.size());
                                getOrders(0);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();
                        Toast.makeText(ActiveOrdersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static final String TAG = "ActiveOrdersActivity";

    private void getOrders(int i) {
        OrderModel order = orders.get(i);
        Log.d(TAG, "getOrders: " + order.requestID);
        Constants.databaseReference().child(Constants.REQUESTS_REPLY).child(order.requestID)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            RequestModel requestModel = dataSnapshot.getValue(RequestModel.class);
                            requestModel.key = order.requestID;
                            Log.d(TAG, "getOrders: " + requestModel.userID);
                            Log.d(TAG, "getOrders: " + order.userID);
                            if (requestModel.userID.equals(order.userID) && !Constants.auth().getCurrentUser().getUid().equals(order.userID)) {
                                list.add(requestModel);
                            } else getRequest(order);
                        }
                    }
                    if (i == orders.size() - 1) {
                        if (!isDestroyed() && !isFinishing()) {
                            Constants.dismissDialog();
                            adapter = new ActiveOrdersAdapter(ActiveOrdersActivity.this, list);
                            binding.recycler.setAdapter(adapter);
                        }
                    } else getOrders(i + 1);
                });
    }

    private void getRequest(OrderModel order) {
        Log.d(TAG, "getRequest: ");
        Constants.databaseReference().child(Constants.REQUESTS).child(order.userID).child(order.requestID)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        RequestModel requestModel = snapshot.getValue(RequestModel.class);
                        requestModel.key = order.requestID;
                        list.add(requestModel);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.destroy();
    }
}