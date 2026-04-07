package com.organation.organation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    
    private Context context;
    private List<RequestModel> requestList;
    private String userType; // "recipient" or "hospital"
    private OnItemClickListener onItemClickListener;
    private OnDonorMatchListener onDonorMatchListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    
    public interface OnDonorMatchListener {
        void onFindDonorMatch(RequestModel request);
    }

    public RequestAdapter(Context context, List<RequestModel> requestList, String userType) {
        this.context = context;
        this.requestList = requestList;
        this.userType = userType;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnDonorMatchListener(OnDonorMatchListener listener) {
        this.onDonorMatchListener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestModel request = requestList.get(position);
        
        if (request == null) {
            // Skip if request is null
            return;
        }
        
        // Set basic request information with null checks
        holder.tvOrganType.setText(getSafeString(request.getOrganType(), "Unknown Organ"));
        holder.tvHospitalName.setText(getSafeString(request.getHospitalName(), "Unknown Hospital"));
        holder.tvRequestDate.setText(getSafeString(request.getRequestDate(), "Unknown Date"));
        holder.tvStatus.setText(getSafeString(request.getStatusDisplay(), "Unknown Status"));
        
        // Set status color safely
        try {
            holder.tvStatus.setTextColor(context.getResources().getColor(request.getStatusColor()));
        } catch (Exception e) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
        }
        
        // Set recipient information with null checks
        holder.tvRecipientName.setText(getSafeString(request.getRecipientName(), "Unknown Recipient"));
        holder.tvBloodType.setText("Blood: " + getSafeString(request.getBloodType(), "Unknown"));
        holder.tvUrgency.setText("Urgency: " + getSafeString(request.getUrgency(), "Unknown"));
        
        // Set additional details based on user type with null checks
        if ("hospital".equals(userType)) {
            holder.tvAdditionalInfo.setText("Aadhaar: " + getSafeString(request.getRecipientAadhaar(), "N/A"));
            holder.tvTreatingDoctor.setText("Doctor: " + getSafeString(request.getTreatingDoctor(), "N/A"));
        } else {
            holder.tvAdditionalInfo.setText("Location: " + getSafeString(request.getHospitalCity(), "N/A"));
            holder.tvTreatingDoctor.setText("Doctor: " + getSafeString(request.getTreatingDoctor(), "N/A"));
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
        
        // Set donor match button click listener (only for hospital users)
        if ("hospital".equals(userType) && holder.btnFindDonors != null) {
            holder.btnFindDonors.setOnClickListener(v -> {
                if (onDonorMatchListener != null) {
                    onDonorMatchListener.onFindDonorMatch(request);
                }
            });
            
            // Show the button for hospital users
            holder.btnFindDonors.setVisibility(View.VISIBLE);
        } else if (holder.btnFindDonors != null) {
            // Hide the button for non-hospital users
            holder.btnFindDonors.setVisibility(View.GONE);
        }
    }

    private String getSafeString(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrganType, tvHospitalName, tvRequestDate, tvStatus;
        TextView tvRecipientName, tvBloodType, tvUrgency;
        TextView tvAdditionalInfo, tvTreatingDoctor;
        Button btnFindDonors;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Basic request info
            tvOrganType = itemView.findViewById(R.id.tvOrganType);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            
            // Recipient info
            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvBloodType = itemView.findViewById(R.id.tvBloodType);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            
            // Additional info
            tvAdditionalInfo = itemView.findViewById(R.id.tvAdditionalInfo);
            tvTreatingDoctor = itemView.findViewById(R.id.tvTreatingDoctor);
            
            // Donor match button
            btnFindDonors = itemView.findViewById(R.id.btnFindDonors);
        }
    }
}
