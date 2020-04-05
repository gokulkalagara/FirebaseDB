package com.frost.firebasedb;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.frost.firebasedb.databinding.ActivityLoginBinding;
import com.frost.firebasedb.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setFireBaseAuth();
        setUp();
    }

    private void setFireBaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
        if (firebaseAuth.getCurrentUser() != null) {
            Toast.makeText(LoginActivity.this,
                    "Active: " + firebaseAuth.getCurrentUser().getEmail(),
                    Toast.LENGTH_SHORT).show();

            gotoHome(Utility.getString("userType", this));
        }
    }

    private void gotoHome(String userType) {
        if (userType == null)
            return;
        switch (userType) {
            case "STUDENT":
                startActivity(new Intent(LoginActivity.this, StudentActivity.class));
                finish();
                break;
            case "ADMIN":
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                break;
            case "DRIVER":
                startActivity(new Intent(LoginActivity.this, DriverActivity.class));
                finish();
                break;
            default:
                firebaseAuth.signOut();
                break;
        }

    }

    private void setUp() {

//        binding.etEmail.setText("gokulkalagara@gmail.com");
//        binding.etPassword.setText("123456");

        binding.tvLogin.setOnClickListener(v -> {
            if (doValidation()) {
                doLogin();
            }
        });

        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });


    }

    private void doLogin() {
        showLoader(true);
        firebaseAuth.signInWithEmailAndPassword(binding.etEmail.getText().toString(),
                binding.etPassword.getText().toString())
                .addOnCompleteListener(this, task -> {
                    showLoader(false);
                    if (task.isSuccessful()) {
                        Utility.showSnackBar(LoginActivity.this, binding.getRoot(), "Successfully user login", 1);
                        fetchUserDetails();
                    } else {
                        task.getException().printStackTrace();
                        Utility.showSnackBar(LoginActivity.this, binding.getRoot(), "Authentication Failed, may be invalid username or password", 0);
                    }

                });
    }

    private void fetchUserDetails() {
        showLoader(true);
        usersReference.child(firebaseAuth.getCurrentUser().getUid()).getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showLoader(false);
                    User user = dataSnapshot.getValue(User.class);
                    Utility.saveString("userType", user.getType(), LoginActivity.this);
                    Utility.saveString("userName", binding.etEmail.getText().toString(), LoginActivity.this);
                    Utility.saveString("password", binding.etPassword.getText().toString(), LoginActivity.this);

                    gotoHome(dataSnapshot.getValue(User.class).getType());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utility.showSnackBar(LoginActivity.this,
                        binding.getRoot(), "Failed get profile: " + databaseError.getMessage(),
                        0);
                showLoader(false);

            }
        });
    }

    private boolean doValidation() {
        Utility.hideKeyboard(this);
        if (TextUtils.isEmpty(binding.etEmail.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Email should not be empty", 2);
            return false;
        }

        if (TextUtils.isEmpty(binding.etPassword.getText().toString())) {
            Utility.showSnackBar(this, binding.getRoot(), "Password should not be empty", 2);
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
        return true;
    }


    private void showLoader(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.tvLogin.setVisibility(show ? View.GONE : View.VISIBLE);
    }

}
