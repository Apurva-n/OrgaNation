package com.organation.organation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DonorMatchAdapter extends RecyclerView.Adapter<DonorMatchAdapter.DonorMatchViewHolder> {
    
    private Context context;
    private List<MLDonorMatchingService.DonorMatchResult> donorMatches;
    private OnDonorActionListener onDonorActionListener;
    private DecimalFormat decimalFormat;
    
    public interface OnDonorActionListener {
        void onCallDonor(String phoneNumber);
        void onEmailDonor(String emailAddress);
        void onViewDonorDetails(DonorModel donor);
    }
    
    public DonorMatchAdapter(Context context, List<MLDonorMatchingService.DonorMatchResult> donorMatches, 
                           OnDonorActionListener listener) {
        this.context = context;
        this.donorMatches = donorMatches != null ? donorMatches : new ArrayList<>();
        this.onDonorActionListener = listener;
        this.decimalFormat = new DecimalFormat("#.##");
    }
    
    public void updateDonorMatches(List<MLDonorMatchingService.DonorMatchResult> newMatches) {
        this.donorMatches = newMatches != null ? newMatches : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public DonorMatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor_match, parent, false);
        return new DonorMatchViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DonorMatchViewHolder holder, int position) {
        MLDonorMatchingService.DonorMatchResult matchResult = donorMatches.get(position);
        DonorModel donor = matchResult.donor;
        
        // Set donor information
        holder.tvRank.setText("#" + (position + 1));
        holder.tvName.setText(donor.getFullName());
        holder.tvCompatibility.setText(decimalFormat.format(matchResult.compatibilityScore * 100) + "%");
        holder.tvBloodGroup.setText(donor.getBloodGroup());
        holder.tvAge.setText("Age: " + donor.getAge());
        holder.tvGender.setText("Gender: " + donor.getGender());
        holder.tvHeightWeight.setText("H: " + donor.getHeight() + "cm | W: " + donor.getWeight() + "kg");
        holder.tvAddress.setText(donor.getFullAddress());
        holder.tvOrgans.setText("Organs: " + donor.getOrgansToDonate());
        
        // Set compatibility score color based on percentage
        double score = matchResult.compatibilityScore * 100;
        if (score >= 80) {
            holder.tvCompatibility.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (score >= 60) {
            holder.tvCompatibility.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.tvCompatibility.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // Set click listeners
        holder.btnCall.setOnClickListener(v -> {
            if (onDonorActionListener != null) {
                onDonorActionListener.onCallDonor(donor.getPhone());
            }
        });
        
        holder.btnEmail.setOnClickListener(v -> {
            if (onDonorActionListener != null) {
                onDonorActionListener.onEmailDonor(donor.getEmail());
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (onDonorActionListener != null) {
                onDonorActionListener.onViewDonorDetails(donor);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return donorMatches.size();
    }
    
    static class DonorMatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvCompatibility, tvBloodGroup, tvAge, tvGender;
        TextView tvHeightWeight, tvAddress, tvOrgans;
        Button btnCall, btnEmail;
        
        public DonorMatchViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvCompatibility = itemView.findViewById(R.id.tvCompatibility);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvHeightWeight = itemView.findViewById(R.id.tvHeightWeight);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvOrgans = itemView.findViewById(R.id.tvOrgans);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnEmail = itemView.findViewById(R.id.btnEmail);
        }
    }
}
