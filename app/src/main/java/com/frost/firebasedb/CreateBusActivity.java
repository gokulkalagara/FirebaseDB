package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.frost.firebasedb.databinding.ActivityCreateBusBinding;
import com.frost.firebasedb.models.Bus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateBusActivity extends AppCompatActivity {

    private ActivityCreateBusBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference busesReference;
    private long id;
    private Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getParcelableExtra("Bus") != null) {
            bus = getIntent().getParcelableExtra("Bus");
        }
        binding = DataBindingUtil.setContentView(CreateBusActivity.this, R.layout.activity_create_bus);
        setUpFireBaseDatabase();

        setUp();

    }

    private void setUpFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        busesReference = firebaseDatabase.getReference("buses");


        busesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    id = dataSnapshot.getChildrenCount();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUp() {

        if (bus != null) {
            binding.tvTitle.setText("Update Bus");
            binding.etBusName.setText(bus.getName());
            binding.etBusNumber.setText(bus.getRegistrationNumber());
            binding.rbActive.setChecked(bus.isStatus());
            binding.rbInActive.setChecked(!bus.isStatus());
            binding.tvCreate.setText("Update");
            binding.imgDelete.setVisibility(View.VISIBLE);
        }
        binding.imgClose.setOnClickListener(v -> {
            onBackPressed();
        });


        binding.tvCreate.setOnClickListener(v -> {
            if (doValidation()) {
                createBus();
            }
        });

        binding.imgDelete.setOnClickListener(v -> {
            if (!Utility.isNetworkAvailable(this)) {
                Utility.showSnackBar(this, binding.getRoot(), "Please check internet connection", 2);
                return;
            }
            AlertDialog.Builder builder = new AlertDialog
                    .Builder(CreateBusActivity.this)
                    .setTitle("Delete")
                    .setMessage("Do you want delete this bus?")
                    .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            deleteBus();
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


            builder.show();
        });

    }

    private void createBus() {
        showLoader(true);
        if (bus.getBusId() != null) {

            String key = bus.getBusId();
            bus.setBusId(null);
            busesReference.child(key).setValue(bus).addOnCompleteListener(this, task -> {

                showLoader(false);
                if (task.isSuccessful()) {
                    Utility.showSnackBar(CreateBusActivity.this, binding.getRoot(),
                            "Bus updated Successfully", 1);
                    onBackPressed();
                } else {
                    task.getException().printStackTrace();
                    Utility.showSnackBar(CreateBusActivity.this, binding.getRoot(),
                            "Failed to update bus : " + task.getException().getMessage(), 0);
                }

            });
            return;
        }

        busesReference.child("" + (id + 1)).setValue(bus).addOnCompleteListener(this, task -> {
            showLoader(false);
            if (task.isSuccessful()) {
                Utility.showSnackBar(CreateBusActivity.this, binding.getRoot(),
                        "Bus created Successfully", 1);
            } else {
                task.getException().printStackTrace();
                Utility.showSnackBar(CreateBusActivity.this, binding.getRoot(),
                        "Failed to create bus : " + task.getException().getMessage(), 0);
            }

        });

    }

    private void deleteBus() {
        showLoader(true);
        busesReference.child(bus.getBusId()).removeValue().addOnCompleteListener(this, task -> {
            showLoader(false);
            if (task.isSuccessful()) {
                Utility.showSnackBar(CreateBusActivity.this, binding.getRoot(),
                        "Bus deleted Successfully", 1);
                onBackPressed();
            } else {
                task.getException().printStackTrace();
                Utility.showSnackBar(CreateBusActivity.this, binding.getRoot(),
                        "Failed to delete bus : " + task.getException().getMessage(), 0);
            }
        });
    }

    private boolean doValidation() {
        if (TextUtils.isEmpty(binding.etBusName.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Bus name should not be empty", 2);
            return false;
        }
        if (TextUtils.isEmpty(binding.etBusNumber.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Registration number should not be empty", 2);
            return false;
        }
        if (!Utility.isNetworkAvailable(this)) {
            Utility.showSnackBar(this, binding.getRoot(), "Please check internet connection", 2);
            return false;
        }
        if (bus == null) {
            bus = new Bus(binding.etBusName.getText().toString(),
                    binding.etBusNumber.getText().toString(),
                    binding.rbActive.isChecked());
        } else {
            bus.setName(binding.etBusName.getText().toString());
            bus.setRegistrationNumber(binding.etBusNumber.getText().toString());
            bus.setStatus(binding.rbActive.isChecked());
        }
        return true;
    }


    private void showLoader(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.tvCreate.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
