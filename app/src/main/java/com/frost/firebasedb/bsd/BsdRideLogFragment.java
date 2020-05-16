package com.frost.firebasedb.bsd;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.frost.firebasedb.R;
import com.frost.firebasedb.adapters.RideLogAdapter;
import com.frost.firebasedb.databinding.FragmentBsdRideLogBinding;
import com.frost.firebasedb.models.RideLog;
import com.frost.firebasedb.models.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BsdRideLogFragment#} factory method to
 * create an instance of this fragment.
 */
public class BsdRideLogFragment extends BottomSheetDialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private User user;
    private FragmentBsdRideLogBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private List<RideLog> rideLogs;

    public BsdRideLogFragment(User user) {
        this.user = user;
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bsd_ride_log, container, false);

        setUpFireBaseDatabase();
        setUp();
        return binding.getRoot();
    }


    private void setUpFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
    }

    private void setUp() {

        binding.imgClose.setOnClickListener(v -> {
            dismiss();
        });

        if (user == null)
            return;


        if (user.getId() != null) {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            binding.progressBar.setVisibility(View.VISIBLE);

            fetchRideLogs();

        }

    }

    private void fetchRideLogs() {

        usersReference.child(user.getId()).child("rideLogs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rideLogs = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    RideLog rideLog = postSnapshot.getValue(RideLog.class);
                    rideLogs.add(rideLog);
                }
                binding.progressBar.setVisibility(View.GONE);
                Collections.reverse(rideLogs);
                binding.recyclerView.setAdapter(new RideLogAdapter(rideLogs));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (dialog == null)
            return super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(bottomSheetDialog -> {
            BottomSheetDialog d = (BottomSheetDialog) bottomSheetDialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            // Right here!
            BottomSheetBehavior.from(bottomSheet)
                    .setState(BottomSheetBehavior.STATE_EXPANDED);
        });


        return dialog;
    }

}
