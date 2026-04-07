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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {
    private Context context;
    private List<DonorModel> donorList;

    public DonorAdapter(Context context, List<DonorModel> donorList) {
        this.context = context;
        this.donorList = donorList;
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor_card, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        DonorModel donor = donorList.get(position);
        holder.bind(donor, context);
    }

    @Override
    public int getItemCount() {
        return donorList.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHeroQuote;
        private TextView tvDonorName;
        private LinearLayout llBloodGroup;
        private TextView tvBloodGroup;
        private LinearLayout llOrgans;
        private TextView tvOrgans;
        private LinearLayout llBasicInfo;
        private TextView tvAge;
        private TextView tvGender;
        private LinearLayout llContactInfo;
        private TextView tvEmail;
        private ImageView ivEmailIcon;
        private TextView tvPhone;
        private ImageView ivPhoneIcon;
        private LinearLayout llAddressInfo;
        private TextView tvAddress;
        private LinearLayout llMedicalInfo;
        private TextView tvMedicalConditions;
        private TextView tvPreviousSurgeries;
        private LinearLayout llEmergencyContact;
        private TextView tvEmergencyName;
        private TextView tvEmergencyRelation;

        public DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeroQuote = itemView.findViewById(R.id.tvHeroQuote);
            tvDonorName = itemView.findViewById(R.id.tvDonorName);
            llBloodGroup = itemView.findViewById(R.id.llBloodGroup);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            llOrgans = itemView.findViewById(R.id.llOrgans);
            tvOrgans = itemView.findViewById(R.id.tvOrgans);
            llBasicInfo = itemView.findViewById(R.id.llBasicInfo);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvGender = itemView.findViewById(R.id.tvGender);
            llContactInfo = itemView.findViewById(R.id.llContactInfo);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            ivEmailIcon = itemView.findViewById(R.id.ivEmailIcon);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            ivPhoneIcon = itemView.findViewById(R.id.ivPhoneIcon);
            llAddressInfo = itemView.findViewById(R.id.llAddressInfo);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            llMedicalInfo = itemView.findViewById(R.id.llMedicalInfo);
            tvMedicalConditions = itemView.findViewById(R.id.tvMedicalConditions);
            tvPreviousSurgeries = itemView.findViewById(R.id.tvPreviousSurgeries);
            llEmergencyContact = itemView.findViewById(R.id.llEmergencyContact);
            tvEmergencyName = itemView.findViewById(R.id.tvEmergencyName);
            tvEmergencyRelation = itemView.findViewById(R.id.tvEmergencyRelation);
        }

        public void bind(DonorModel donor, Context context) {
            // Hero quote - inspiring message
            tvHeroQuote.setText("🌟 A Hero Among Us 🌟");

            // Donor Name - Prominent
            tvDonorName.setText(donor.getFullName());

            // Blood Group - Important for matching
            tvBloodGroup.setText("🔴 " + donor.getBloodGroup());

            // Organs willing to donate
            String organsText = donor.getOrgansToNDonate();
            if (organsText != null && !organsText.isEmpty() && !organsText.equals("N/A")) {
                tvOrgans.setText("💚 " + organsText);
                llOrgans.setVisibility(View.VISIBLE);
            } else {
                llOrgans.setVisibility(View.GONE);
            }

            // Basic Info
            tvAge.setText("Age: " + donor.getAge() + " yrs");
            tvGender.setText("Gender: " + donor.getGender());

            // Contact Information with clickable actions
            tvEmail.setText(donor.getEmail());
            tvEmail.setOnClickListener(v -> sendEmail(donor.getEmail(), context));
            ivEmailIcon.setOnClickListener(v -> sendEmail(donor.getEmail(), context));

            tvPhone.setText(donor.getPhone());
            tvPhone.setOnClickListener(v -> callDonor(donor.getPhone(), context));
            ivPhoneIcon.setOnClickListener(v -> callDonor(donor.getPhone(), context));

            // Address Information
            StringBuilder addressBuilder = new StringBuilder();
            addressBuilder.append(donor.getStreet()).append(", ");
            addressBuilder.append(donor.getLandmark()).append(", ");
            addressBuilder.append(donor.getCity()).append(", ");
            addressBuilder.append(donor.getState());
            tvAddress.setText("📍 " + addressBuilder.toString());

            // Medical Information
            String medicalConditions = donor.getMedicalConditions();
            if (medicalConditions != null && !medicalConditions.isEmpty() && !medicalConditions.equals("N/A")) {
                tvMedicalConditions.setText("Medical Conditions: " + medicalConditions);
                tvMedicalConditions.setVisibility(View.VISIBLE);
            } else {
                tvMedicalConditions.setVisibility(View.GONE);
            }

            String previousSurgeries = donor.getPreviousSurgeries();
            if (previousSurgeries != null && !previousSurgeries.isEmpty() && !previousSurgeries.equals("N/A")) {
                tvPreviousSurgeries.setText("Previous Surgeries: " + previousSurgeries);
                tvPreviousSurgeries.setVisibility(View.VISIBLE);
            } else {
                tvPreviousSurgeries.setVisibility(View.GONE);
            }

            // Emergency Contact (if available)
            if (donor.getEmergencyContact() != null && !donor.getEmergencyContact().isEmpty()) {
                String emergencyName = donor.getEmergencyContact().get("name");
                String emergencyRelation = donor.getEmergencyContact().get("relation");
                if (emergencyName != null && !emergencyName.equals("N/A")) {
                    tvEmergencyName.setText("Emergency Contact: " + emergencyName);
                    tvEmergencyRelation.setText("Relation: " + emergencyRelation);
                    llEmergencyContact.setVisibility(View.VISIBLE);
                } else {
                    llEmergencyContact.setVisibility(View.GONE);
                }
            } else {
                llEmergencyContact.setVisibility(View.GONE);
            }
        }

        private void sendEmail(String email, Context context) {
            if (email != null && !email.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Organ Donation Inquiry");
                context.startActivity(intent);
            }
        }

        private void callDonor(String phone, Context context) {
            if (phone != null && !phone.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                context.startActivity(intent);
            }
        }
    }
}
