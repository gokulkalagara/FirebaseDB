package com.frost.firebasedb.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frost.firebasedb.CreateBusActivity;
import com.frost.firebasedb.LocationActivity;
import com.frost.firebasedb.MainActivity;
import com.frost.firebasedb.ProfileActivity;
import com.frost.firebasedb.R;
import com.frost.firebasedb.Utility;
import com.frost.firebasedb.adapters.BusAdapter;
import com.frost.firebasedb.databinding.FragmentBusesBinding;
import com.frost.firebasedb.interfaces.IBusAdapter;
import com.frost.firebasedb.models.Bus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BusesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BusesFragment extends Fragment implements IBusAdapter {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentBusesBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference busesReference;
    private List<Bus> busList;
    private BusAdapter busAdapter;

    public BusesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BusesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BusesFragment newInstance(String param1, String param2) {
        BusesFragment fragment = new BusesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpFireBaseDatabase();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_buses, container, false);
        setUp();
        return binding.getRoot();
    }

    private void setUpFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        busesReference = firebaseDatabase.getReference("buses");


    }

    private void setUp() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


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
                binding.recyclerView.setAdapter(busAdapter = new BusAdapter(busList, BusesFragment.this));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClickBus(Bus bus, int position) {
        switch (Utility.getString("userType", getActivity())) {
            case "ADMIN":
                Intent intent = new Intent(getActivity(), CreateBusActivity.class);
                intent.putExtra("Bus", bus);
                startActivity(intent);
                break;

            default:
                viewLocation(bus, position);
                break;

        }

    }

    @Override
    public void viewLocation(Bus bus, int position) {
        Intent intent = new Intent(getActivity(), LocationActivity.class);
        intent.putExtra("Bus", bus);
        startActivity(intent);
    }
}
