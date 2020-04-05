package com.frost.firebasedb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.frost.firebasedb.databinding.ActivityCreateAdminBinding;
import com.frost.firebasedb.databinding.ActivitySignUpBinding;
import com.frost.firebasedb.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAdminActivity extends AppCompatActivity {

    private ActivityCreateAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private User user;
    private String type = "ADMIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_admin);
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

        user = new User();
        user.setFullName(binding.etName.getText().toString());
        user.setEmail(binding.etEmail.getText().toString());
        user.setMobileNumber(binding.etMobile.getText().toString());
        user.setType(type);
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
                        Utility.showSnackBar(CreateAdminActivity.this, binding.getRoot(), "Successfully admin created", 1);
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
                Utility.showSnackBar(CreateAdminActivity.this, binding.getRoot(), "Failed to register: " + task.getException().getMessage(), 0);
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
