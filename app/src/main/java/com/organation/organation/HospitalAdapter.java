package com.organation.organation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {
    private Context context;
    private List<HospitalModel> hospitalList;

    public HospitalAdapter(Context context, List<HospitalModel> hospitalList) {
        this.context = context;
        this.hospitalList = hospitalList;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hospital_card, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        HospitalModel hospital = hospitalList.get(position);
        holder.bind(hospital, context);
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHospitalBadge;
        private TextView tvHospitalName;
        private LinearLayout llHospitalType;
        private TextView tvHospitalType;
        private LinearLayout llAuthorityInfo;
        private TextView tvAuthorityName;
        private LinearLayout llContactInfo;
        private TextView tvOfficialEmail;
        private ImageView ivEmailIcon;
        private TextView tvContactNumber;
        private ImageView ivPhoneIcon;
        private LinearLayout llAddressInfo;
        private TextView tvAddress;
        private LinearLayout llRegInfo;
        private TextView tvGovRegNumber;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalBadge = itemView.findViewById(R.id.tvHospitalBadge);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            llHospitalType = itemView.findViewById(R.id.llHospitalType);
            tvHospitalType = itemView.findViewById(R.id.tvHospitalType);
            llAuthorityInfo = itemView.findViewById(R.id.llAuthorityInfo);
            tvAuthorityName = itemView.findViewById(R.id.tvAuthorityName);
            llContactInfo = itemView.findViewById(R.id.llContactInfo);
            tvOfficialEmail = itemView.findViewById(R.id.tvOfficialEmail);
            ivEmailIcon = itemView.findViewById(R.id.ivEmailIcon);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            ivPhoneIcon = itemView.findViewById(R.id.ivPhoneIcon);
            llAddressInfo = itemView.findViewById(R.id.llAddressInfo);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            llRegInfo = itemView.findViewById(R.id.llRegInfo);
            tvGovRegNumber = itemView.findViewById(R.id.tvGovRegNumber);
        }

        public void bind(HospitalModel hospital, Context context) {
            // Hospital Badge
            tvHospitalBadge.setText("🏥 Medical Excellence 🏥");

            // Hospital Name - Prominent
            tvHospitalName.setText(hospital.getHospitalName());

            // Hospital Type
            String hospitalType = hospital.getHospitalType();
            if (hospitalType != null && !hospitalType.isEmpty() && !hospitalType.equals("N/A")) {
                tvHospitalType.setText("🏥 " + hospitalType);
                llHospitalType.setVisibility(View.VISIBLE);
            } else {
                llHospitalType.setVisibility(View.GONE);
            }

            // Authority Information
            String authorityName = hospital.getAuthorityName();
            if (authorityName != null && !authorityName.isEmpty() && !authorityName.equals("N/A")) {
                tvAuthorityName.setText("👨‍⚕️ " + authorityName);
                llAuthorityInfo.setVisibility(View.VISIBLE);
            } else {
                llAuthorityInfo.setVisibility(View.GONE);
            }

            // Contact Information with clickable actions
            tvOfficialEmail.setText(hospital.getOfficialEmail());
            tvOfficialEmail.setOnClickListener(v -> sendEmail(hospital.getOfficialEmail(), context));
            ivEmailIcon.setOnClickListener(v -> sendEmail(hospital.getOfficialEmail(), context));

            tvContactNumber.setText(hospital.getContactNumber());
            tvContactNumber.setOnClickListener(v -> callHospital(hospital.getContactNumber(), context));
            ivPhoneIcon.setOnClickListener(v -> callHospital(hospital.getContactNumber(), context));

            // Address Information
            StringBuilder addressBuilder = new StringBuilder();
            if (hospital.getStreet() != null && !hospital.getStreet().equals("N/A")) {
                addressBuilder.append(hospital.getStreet());
            }
            if (hospital.getLandmark() != null && !hospital.getLandmark().equals("N/A")) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(hospital.getLandmark());
            }
            if (hospital.getCity() != null && !hospital.getCity().equals("N/A")) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(hospital.getCity());
            }
            if (hospital.getState() != null && !hospital.getState().equals("N/A")) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(hospital.getState());
            }
            if (hospital.getPincode() != null && !hospital.getPincode().equals("N/A")) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(hospital.getPincode());
            }

            if (addressBuilder.length() > 0) {
                tvAddress.setText("📍 " + addressBuilder.toString());
                llAddressInfo.setVisibility(View.VISIBLE);
            } else {
                llAddressInfo.setVisibility(View.GONE);
            }

            // Registration Number
            String regNumber = hospital.getGovRegNumber();
            if (regNumber != null && !regNumber.isEmpty() && !regNumber.equals("N/A")) {
                tvGovRegNumber.setText("Gov. Reg. No: " + regNumber);
                llRegInfo.setVisibility(View.VISIBLE);
            } else {
                llRegInfo.setVisibility(View.GONE);
            }
        }

        private void sendEmail(String email, Context context) {
            if (email != null && !email.equals("N/A") && !email.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + email));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Hospital Inquiry - OrgaNation");
                    intent.putExtra(Intent.EXTRA_TEXT, "Dear Hospital Authority,\n\nI am contacting you regarding organ donation services.\n\nBest regards,\n[Your Name]");
                    context.startActivity(intent);
                } catch (Exception e) {
                    // Fallback to basic email intent
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + email));
                    context.startActivity(intent);
                }
            }
        }

        private void callHospital(String phone, Context context) {
            if (phone != null && !phone.equals("N/A") && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                context.startActivity(intent);
            }
        }
    }
}
