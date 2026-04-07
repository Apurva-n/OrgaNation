package com.organation.organation;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Donor_main_page extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_main_page);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // This creates the 'hamburger' icon and handles the sliding animation
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_available_donors) {
                // Navigate to Available Donors Activity
                startActivity(new Intent(Donor_main_page.this, AvailableDonorsActivity.class));
            } else if (id == R.id.nav_available_recipients) {
                // Navigate to Available Recipients Activity
                startActivity(new Intent(Donor_main_page.this, AvailableRecipientsActivity.class));
            } else if (id == R.id.nav_update_profile) {
                // Take them back to Registration to edit details
                startActivity(new Intent(Donor_main_page.this, DonorRegistration.class));
            } else if (id == R.id.nav_logout) {
                // Logout from Firebase
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Donor_main_page.this, Donor_login.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}