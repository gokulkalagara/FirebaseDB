package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.frost.firebasedb.databinding.ActivityProfileBinding;
import com.frost.firebasedb.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        setUpFirebase();

        setUp();
    }

    private void setUpFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
    }

    private void setUp() {

        showLoader(true);
        usersReference.child(firebaseAuth.getCurrentUser().getUid()).getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showLoader(false);
                    updateDetails(dataSnapshot.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utility.showSnackBar(ProfileActivity.this,
                        binding.getRoot(), "Failed get profile: " + databaseError.getMessage(),
                        0);
                showLoader(false);

            }
        });

        binding.llLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Utility.deleteAll(ProfileActivity.this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.imgClose.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void showLoader(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.llProfile.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateDetails(User user) {
        Utility.saveString("userType", user.getType(), this);
        binding.tvEmail.setText(user.getEmail());
        binding.tvName.setText(user.getFullName());
        binding.tvShortName.setText(("" + user.getFullName().charAt(0)).toUpperCase());
    }
}
