package com.frost.firebasedb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.frost.firebasedb.R;
import com.frost.firebasedb.Utility;
import com.frost.firebasedb.databinding.ItemAdminBinding;
import com.frost.firebasedb.interfaces.IAdminAdapter;
import com.frost.firebasedb.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class AdminsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> userList;
    private IAdminAdapter iAdminAdapter;


    public AdminsAdapter(List<User> userList, IAdminAdapter iAdminAdapter) {
        this.userList = userList;
        this.iAdminAdapter = iAdminAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdminViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_admin, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof AdminViewHolder) {
            AdminViewHolder adminViewHolder = (AdminViewHolder) holder;
            adminViewHolder.bindData(userList.get(position), position);
            adminViewHolder.binding.tvDelete.setOnClickListener(v -> {
                iAdminAdapter.deleteAdmin(userList.get(position), position);
            });
            adminViewHolder.binding.llCall.setOnClickListener(v -> {
                iAdminAdapter.doCall(userList.get(position), position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class AdminViewHolder extends RecyclerView.ViewHolder {

        private ItemAdminBinding binding;

        public AdminViewHolder(@NonNull ItemAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(User user, int position) {
            binding.tvAdminName.setText(user.getFullName());
            binding.tvNumber.setText(user.getMobileNumber());

            binding.tvDelete.setVisibility(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.id) ? View.GONE : View.VISIBLE);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(Utility.dpSize(binding.getRoot().getContext(), 15), Utility.dpSize(binding.getRoot().getContext(), 15),
                    Utility.dpSize(binding.getRoot().getContext(), 15),
                    position == userList.size() - 1 ? Utility.dpSize(binding.getRoot().getContext(), 120) : 0);
            binding.getRoot().setLayoutParams(params);
        }
    }
}
