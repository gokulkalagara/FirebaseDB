package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.frost.firebasedb.adapters.BusAdapter;
import com.frost.firebasedb.databinding.ActivityMainBinding;
import com.frost.firebasedb.fragments.AdminsFragment;
import com.frost.firebasedb.fragments.BusesFragment;
import com.frost.firebasedb.fragments.DriversFragment;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUp();
    }


    private void setUp() {

        binding.viewPager.setOffscreenPageLimit(3);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));

        binding.fabAdd.setOnClickListener(v -> {
            switch (binding.viewPager.getCurrentItem()) {
                case 0: // BUS
                    startActivity(new Intent(MainActivity.this, CreateBusActivity.class));
                    break;
                case 1: // DRIVER
                    startActivity(new Intent(MainActivity.this, CreateDriverActivity.class));
                    break;
                case 2: // DRIVER
                    startActivity(new Intent(MainActivity.this, CreateAdminActivity.class));
                    break;

            }
        });


        binding.imgProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

    }


    @Override
    public void onClickBus(Bus bus, int position) {
        Intent intent = new Intent(this, CreateBusActivity.class);
        intent.putExtra("Bus", bus);
        startActivity(intent);
    }

    @Override
    public void viewLocation(Bus bus, int position) {
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("Bus", bus);
        startActivity(intent);
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {


        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = BusesFragment.newInstance(null, null);
                    break;
                case 1:
                    fragment = DriversFragment.newInstance(null, null);
                    break;
                case 2:
                    fragment = AdminsFragment.newInstance(null, null);
                    break;
            }
            return fragment;
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "Buses" : (position == 1 ? "Drivers" : "Admins");
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
