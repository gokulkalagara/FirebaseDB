package com.frost.firebasedb.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.frost.firebasedb.R;
import com.frost.firebasedb.Utility;
import com.frost.firebasedb.databinding.RideLogItemBinding;
import com.frost.firebasedb.models.RideLog;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Created by Gokul Kalagara (Mr. Psycho) on 16-05-2020.
 * <p>
 * Frost
 */
public class RideLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RideLog> list;

    public RideLogAdapter(List<RideLog> list) {
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RideLogViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.ride_log_item,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RideLogViewHolder) {
            RideLogViewHolder rideLogViewHolder = (RideLogViewHolder) holder;
            rideLogViewHolder.bindData(list.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return  list.size();
    }


    private class RideLogViewHolder extends RecyclerView.ViewHolder {
        private RideLogItemBinding binding;

        private RideLogViewHolder(RideLogItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bindData(RideLog rideLog, int position) {
            binding.tvBusName.setText(rideLog.getBusName());
            binding.tvStartTime.setText(rideLog.getStartTime());
            binding.tvEndTime.setText(rideLog.getEndTime() == null ? "--/--" : rideLog.getEndTime());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(Utility.dpSize(binding.getRoot().getContext(), 15), Utility.dpSize(binding.getRoot().getContext(), 15),
                    Utility.dpSize(binding.getRoot().getContext(), 15),
                    position == list.size() - 1 ? Utility.dpSize(binding.getRoot().getContext(), 120) : 0);
            binding.getRoot().setLayoutParams(params);
        }
    }
}
