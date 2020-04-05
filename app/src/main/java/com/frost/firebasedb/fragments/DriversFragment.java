package com.frost.firebasedb.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frost.firebasedb.R;
import com.frost.firebasedb.Utility;
import com.frost.firebasedb.adapters.AdminsAdapter;
import com.frost.firebasedb.databinding.FragmentDriversBinding;
import com.frost.firebasedb.interfaces.IAdminAdapter;
import com.frost.firebasedb.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriversFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriversFragment extends Fragment implements IAdminAdapter {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private List<User> userList;
    private AdminsAdapter adminsAdapter;
    private FragmentDriversBinding binding;

    public DriversFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriversFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriversFragment newInstance(String param1, String param2) {
        DriversFragment fragment = new DriversFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_drivers, container, false);
        setUp();
        return binding.getRoot();
    }


    private void setUpFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
    }

    private void setUp() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchAdmins();
        });

        fetchAdmins();
    }


    private void fetchAdmins() {
        binding.progressBar.setVisibility(View.VISIBLE);
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(postSnapshot.getKey());
                        if (user.getType().equals("DRIVER"))
                            userList.add(user);
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.recyclerView.setAdapter(adminsAdapter = new AdminsAdapter(userList, DriversFragment.this));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void deleteAdmin(User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete");
        builder.setMessage("Do you want to delete this admin ?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.id)) {
                Utility.showSnackBar(getActivity(),
                        binding.getRoot(), "You can't be delete yourself",
                        0);
                return;
            }
            deleteAdmin(user);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();

    }

    private void deleteAdmin(User user) {
        showLoader(true);
        usersReference.child(user.getId()).removeValue().addOnCompleteListener(getActivity(), task -> {
            showLoader(false);
            if (task.isSuccessful()) {
                Utility.showSnackBar(getActivity(), binding.getRoot(),
                        "Admin deleted Successfully", 1);
            } else {
                task.getException().printStackTrace();
                Utility.showSnackBar(getActivity(), binding.getRoot(),
                        "Failed to delete Admin : " + task.getException().getMessage(), 0);
            }
        });
    }

    private void showLoader(boolean show) {
        binding.progressBarBottom.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void doCall(User user, int position) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + user.getMobileNumber()));
        startActivity(intent);
    }
}
