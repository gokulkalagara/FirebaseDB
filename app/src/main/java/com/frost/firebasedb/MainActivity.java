package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.frost.firebasedb.adapters.BusAdapter;
import com.frost.firebasedb.databinding.ActivityMainBinding;
import com.frost.firebasedb.interfaces.IBusAdapter;
import com.frost.firebasedb.models.Bus;
import com.frost.firebasedb.models.Location;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IBusAdapter {

    private ActivityMainBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference busesReference;
    private List<Bus> busList;
    private BusAdapter busAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUpFireBaseDatabase();
        setUp();
    }

    private void setUpFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        busesReference = firebaseDatabase.getReference("buses");


    }


    private void setUp() {

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateBusActivity.class));
        });


        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchBuses();
        });

        fetchBuses();
    }

    private void fetchBuses() {
        binding.progressBar.setVisibility(View.VISIBLE);
        busesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                busList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Bus bus = postSnapshot.getValue(Bus.class);
                    bus.setBusId(postSnapshot.getKey());
                    busList.add(bus);
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.recyclerView.setAdapter(busAdapter = new BusAdapter(busList, MainActivity.this));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onClickBus(Bus bus, int position) {
        Intent intent = new Intent(this, CreateBusActivity.class);
        intent.putExtra("Bus", bus);
        startActivity(intent);
    }
}
