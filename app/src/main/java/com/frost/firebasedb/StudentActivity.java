package com.frost.firebasedb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.frost.firebasedb.databinding.ActivityStudentBinding;
import com.frost.firebasedb.fragments.BusesFragment;

public class StudentActivity extends AppCompatActivity {

    private ActivityStudentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_student);
        setUp();
    }

    private void setUp() {
        binding.imgProfile.setOnClickListener(v -> {
            startActivity(new Intent(StudentActivity.this, ProfileActivity.class));
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, BusesFragment.newInstance(null, null)).commit();
    }
}
