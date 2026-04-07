package com.organation.organation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PendingHospitalAdapter extends RecyclerView.Adapter<PendingHospitalAdapter.ViewHolder> {
    
    private Context context;
    private List<HospitalVerificationModel> pendingHospitals;
    private OnHospitalActionListener actionListener;
    
    public PendingHospitalAdapter(Context context, List<HospitalVerificationModel> pendingHospitals, OnHospitalActionListener actionListener) {
        this.context = context;
        this.pendingHospitals = pendingHospitals;
        this.actionListener = actionListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_hospital, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HospitalVerificationModel hospital = pendingHospitals.get(position);
        
        // Set hospital details
        holder.tvHospitalName.setText(hospital.getHospitalName());
        holder.tvAuthorityName.setText(hospital.getAuthorityName());
        holder.tvRegistrationNumber.setText(hospital.getRegistrationNumber());
        holder.tvEmail.setText(hospital.getEmail());
        holder.tvPhone.setText(hospital.getPhone());
        holder.tvAddress.setText(hospital.getStreet() + ", " + hospital.getCity() + ", " + hospital.getState());
        holder.tvHospitalType.setText(hospital.getHospitalType());
        
        // Format registration date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String registrationDate = hospital.getRegistrationDate() != null ? 
                dateFormat.format(hospital.getRegistrationDate()) : "N/A";
        holder.tvRegistrationDate.setText("Registered: " + registrationDate);
        
        // Set status badge
        holder.tvStatus.setText(hospital.getStatus().toUpperCase());
        if (hospital.getStatus().equals("pending")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
        }
        
        // Set click listeners
        holder.btnApprove.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onHospitalAction(hospital, "approve");
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onHospitalAction(hospital, "reject");
            }
        });
        
        holder.btnDetails.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onHospitalAction(hospital, "details");
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return pendingHospitals.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvAuthorityName, tvRegistrationNumber, tvEmail, tvPhone;
        TextView tvAddress, tvHospitalType, tvRegistrationDate, tvStatus;
        Button btnApprove, btnReject, btnDetails;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvAuthorityName = itemView.findViewById(R.id.tvAuthorityName);
            tvRegistrationNumber = itemView.findViewById(R.id.tvRegistrationNumber);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvHospitalType = itemView.findViewById(R.id.tvHospitalType);
            tvRegistrationDate = itemView.findViewById(R.id.tvRegistrationDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
