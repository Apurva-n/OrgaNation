package com.organation.organation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecipientAdapter extends RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder> {
    private final Context context;
    private final List<RecipientModel> recipientList;

    public RecipientAdapter(Context context, List<RecipientModel> recipientList) {
        this.context = context;
        this.recipientList = recipientList;
    }

    @NonNull
    @Override
    public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipient_card, parent, false);
        return new RecipientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipientViewHolder holder, int position) {
        holder.bind(recipientList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return recipientList.size();
    }

    static class RecipientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvBloodGroup;
        TextView tvOrgans;
        TextView tvUrgency;
        TextView tvLocation;
        TextView tvHospital;
        TextView tvPhone;
        ImageView ivPhone;
        TextView tvEmail;
        ImageView ivEmail;
        View contactRow;

        public RecipientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipientName);
            tvBloodGroup = itemView.findViewById(R.id.tvRecipientBloodGroup);
            tvOrgans = itemView.findViewById(R.id.tvRecipientOrgans);
            tvUrgency = itemView.findViewById(R.id.tvRecipientUrgency);
            tvLocation = itemView.findViewById(R.id.tvRecipientLocation);
            tvHospital = itemView.findViewById(R.id.tvRecipientHospital);
            tvPhone = itemView.findViewById(R.id.tvRecipientPhone);
            ivPhone = itemView.findViewById(R.id.ivRecipientPhone);
            tvEmail = itemView.findViewById(R.id.tvRecipientEmail);
            ivEmail = itemView.findViewById(R.id.ivRecipientEmail);
            contactRow = itemView.findViewById(R.id.contactRow);
        }

        public void bind(RecipientModel recipient, Context context) {
            tvName.setText(recipient.getFullName());
            tvBloodGroup.setText("Blood Group: " + recipient.getBloodGroup());
            tvOrgans.setText("Organ Needed: " + recipient.getOrgansNeeded());

            String urgency = recipient.getUrgency();
            tvUrgency.setText((urgency != null && !urgency.isEmpty()) ? ("Urgency: " + urgency) : "Urgency: N/A");

            tvLocation.setText("Location: " + recipient.getCity() + ", " + recipient.getState());

            String hospitalName = recipient.getHospitalDetails().get("01]Hospital Name");
            String doctorName = recipient.getHospitalDetails().get("02]Doctor Name");
            if ((hospitalName == null || hospitalName.isEmpty()) && (doctorName == null || doctorName.isEmpty())) {
                tvHospital.setVisibility(View.GONE);
            } else {
                String hospitalLine = "";
                if (hospitalName != null && !hospitalName.isEmpty()) {
                    hospitalLine += hospitalName;
                }
                if (doctorName != null && !doctorName.isEmpty()) {
                    if (!hospitalLine.isEmpty()) {
                        hospitalLine += " (Dr. " + doctorName + ")";
                    } else {
                        hospitalLine += "Dr. " + doctorName;
                    }
                }
                tvHospital.setText("Hospital: " + hospitalLine);
                tvHospital.setVisibility(View.VISIBLE);
            }

            String phone = recipient.getPhone();
            boolean hasPhone = phone != null && !phone.isEmpty();
            tvPhone.setText(hasPhone ? phone : "");
            ivPhone.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
            tvPhone.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
            if (hasPhone) {
                ivPhone.setOnClickListener(v -> call(phone, context));
                tvPhone.setOnClickListener(v -> call(phone, context));
            }

            String email = recipient.getEmail();
            boolean hasEmail = email != null && !email.isEmpty();
            tvEmail.setText(hasEmail ? email : "");
            ivEmail.setVisibility(hasEmail ? View.VISIBLE : View.GONE);
            tvEmail.setVisibility(hasEmail ? View.VISIBLE : View.GONE);
            if (hasEmail) {
                ivEmail.setOnClickListener(v -> sendEmail(email, context));
                tvEmail.setOnClickListener(v -> sendEmail(email, context));
            }

            // Hide contact row if no contact methods are available
            if (!hasPhone && !hasEmail) {
                contactRow.setVisibility(View.GONE);
            } else {
                contactRow.setVisibility(View.VISIBLE);
            }
        }

        private void call(String phone, Context context) {
            if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                context.startActivity(intent);
            }
        }

        private void sendEmail(String email, Context context) {
            if (email != null && !email.isEmpty() && !email.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Organ Help Request");
                context.startActivity(intent);
            }
        }
    }
}
