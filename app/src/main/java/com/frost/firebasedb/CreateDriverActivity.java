package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.frost.firebasedb.adapters.BusAdapter;
import com.frost.firebasedb.databinding.ActivityCreateAdminBinding;
import com.frost.firebasedb.databinding.ActivityCreateDriverBinding;
import com.frost.firebasedb.fragments.BusesFragment;
import com.frost.firebasedb.models.Bus;
import com.frost.firebasedb.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateDriverActivity extends AppCompatActivity {

    private ActivityCreateDriverBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private DatabaseReference busesReference;
    private User user;
    private List<String> busName;
    private List<Bus> busList;
    private int busPosition = 0;
    private String type = "DRIVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_driver);
        setFireBaseAuth();
        setFireBaseDatabase();
        setUp();
    }


    private void setFireBaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
        busesReference = firebaseDatabase.getReference("buses");
    }


    private void setUp() {

//        binding.etName.setText("Gokul Kalagara");
//        binding.etEmail.setText("gokulkalagara@gmail.com");
//        binding.etMobile.setText("7207824353");
//        binding.etPassword.setText("123456");

        binding.imgClose.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.tvSignUp.setOnClickListener(v -> {
            if (doValidation()) {
                doSignUp();
            }
        });

        fetchBuses();

    }

    private void fetchBuses() {
        binding.progressBar.setVisibility(View.VISIBLE);
        busesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                busList = new ArrayList<>();
                busName = new ArrayList<>();
                busName.add("Please assign bus");
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Bus bus = postSnapshot.getValue(Bus.class);
                    bus.setBusId(postSnapshot.getKey());
                    busList.add(bus);
                    busName.add(bus.getName());
                }
                binding.progressBar.setVisibility(View.GONE);

                ArrayAdapter<String> adapterCourse = new ArrayAdapter<>(CreateDriverActivity.this,
                        android.R.layout.simple_spinner_item, busName);
                adapterCourse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.busSpinner.setAdapter(adapterCourse);


                addSpinnerListener();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addSpinnerListener() {
        binding.busSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                busPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private boolean doValidation() {
        Utility.hideKeyboard(this);

        if (TextUtils.isEmpty(binding.etName.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Name should not be empty", 2);
            return false;
        }
        if (TextUtils.isEmpty(binding.etEmail.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Email should not be empty", 2);
            return false;
        }

        if (TextUtils.isEmpty(binding.etPassword.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Password should not be empty", 2);
            return false;
        }

        if (!Utility.isValidMobile(binding.etMobile.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Invalid mobile number", 2);
            return false;
        }

        if (binding.etPassword.getText().toString().length() < 6) {
            Utility.showSnackBar(this, binding.getRoot(), "Password minimum 6 characters", 2);
            return false;
        }
        if (!Utility.isNetworkAvailable(this)) {
            Utility.showSnackBar(this, binding.getRoot(), "Please check internet connection", 2);
            return false;
        }
        if (busPosition == 0) {
            Utility.showSnackBar(this, binding.getRoot(), "Please assign bus", 2);
            return false;
        }

        user = new User();
        user.setFullName(binding.etName.getText().toString());
        user.setEmail(binding.etEmail.getText().toString());
        user.setMobileNumber(binding.etMobile.getText().toString());
        user.setType(type);
        user.setBusId(Long.parseLong(busList.get(busPosition - 1).getBusId()));
        return true;
    }


    private void doSignUp() {
        showLoader(true);
        firebaseAuth.createUserWithEmailAndPassword(binding.etEmail.getText().toString(),
                binding.etPassword.getText().toString()).addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful()) {
                usersReference.child(task.getResult().getUser().getUid()).setValue(user).addOnCompleteListener(this, addTask -> {
                    if (task.isSuccessful()) {
                        Utility.showSnackBar(CreateDriverActivity.this, binding.getRoot(), "Successfully driver created", 1);
                        showLoader(false);
                        getExistingUser();
                    } else {
                        task.getException().printStackTrace();
                        showLoader(false);
                    }
                });
            } else {
                showLoader(false);
                task.getException().printStackTrace();
                Utility.showSnackBar(CreateDriverActivity.this, binding.getRoot(), "Failed to register: " + task.getException().getMessage(), 0);
            }
        });
    }


    private void showLoader(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.tvSignUp.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    public void getExistingUser() {
        showLoader(true);
        firebaseAuth.signInWithEmailAndPassword(Utility.getString("userName", this), Utility.getString("password", this))
                .addOnCompleteListener(this, task -> {
                    showLoader(false);
                    if (task.isSuccessful()) {
                        onBackPressed();
                    } else {
                        task.getException().printStackTrace();
                    }

                });
    }
}
