package com.frost.firebasedb.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.frost.firebasedb.R;
import com.frost.firebasedb.Utility;
import com.frost.firebasedb.databinding.ItemBusBinding;
import com.frost.firebasedb.interfaces.IBusAdapter;
import com.frost.firebasedb.models.Bus;

import java.util.List;

/**
 * Created by Gokul Kalagara (Mr. Pyscho) on 07-03-2020.
 * <p>
 * FROST
 */
public class BusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Bus> busList;
    private IBusAdapter iBusAdapter;


    public BusAdapter(List<Bus> busList, IBusAdapter iBusAdapter) {
        this.busList = busList;
        this.iBusAdapter = iBusAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BusViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_bus, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof BusViewHolder) {
            BusViewHolder busViewHolder = (BusViewHolder) holder;
            busViewHolder.bindData(busList.get(position), position);
            busViewHolder.itemView.setOnClickListener(v -> {
                iBusAdapter.onClickBus(busList.get(position), position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return busList.size();
    }


    public class BusViewHolder extends RecyclerView.ViewHolder {

        private ItemBusBinding binding;

        public BusViewHolder(@NonNull ItemBusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(Bus bus, int position) {
            binding.tvBusName.setText(bus.getName());
            binding.tvNumber.setText(bus.getRegistrationNumber());
            binding.tvStatus.setText(bus.isStatus() ? "ACTIVE" : "IN-ACTIVE");
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.tvStatus.getContext(),
                    bus.isStatus() ? R.color.button_green : R.color.other_red));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(Utility.dpSize(binding.getRoot().getContext(), 15), Utility.dpSize(binding.getRoot().getContext(), 15),
                    Utility.dpSize(binding.getRoot().getContext(), 15),
                    position == busList.size() - 1 ? Utility.dpSize(binding.getRoot().getContext(), 120) : 0);
            binding.getRoot().setLayoutParams(params);
        }
    }
}
