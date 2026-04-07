package com.organation.organation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardAnalyticsActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTotalHospitals, tvTotalDonors, tvTotalRecipients;
    private TextView tvActiveDonors, tvActiveRecipients, tvTotalMatches;
    private ProgressBar progressBar;
    private CardView cardOverview, cardOrganDemand, cardOrganSupply;

    // Charts
    private PieChart pieChartOrganDemand, pieChartOrganSupply;
    private BarChart barChartUserRegistration;

    // Firebase
    private FirebaseFirestore db;

    // Data variables
    private int totalHospitals = 0, totalDonors = 0, totalRecipients = 0;
    private int activeDonors = 0, activeRecipients = 0, totalMatches = 0;
    private Map<String, Integer> organDemandMap = new HashMap<>();
    private Map<String, Integer> organSupplyMap = new HashMap<>();
    private Map<String, Integer> monthlyRegistrationsMap = new HashMap<>();
    private List<String> months = new ArrayList<>();
    private List<Integer> monthlyRegistrations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_analytics);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        initializeCharts();
        loadAnalyticsData();
    }

    private void initializeViews() {
        // TextViews for statistics
        tvTotalHospitals = findViewById(R.id.tvTotalHospitals);
        tvTotalDonors = findViewById(R.id.tvTotalDonors);
        tvTotalRecipients = findViewById(R.id.tvTotalRecipients);
        tvActiveDonors = findViewById(R.id.tvActiveDonors);
        tvActiveRecipients = findViewById(R.id.tvActiveRecipients);
        tvTotalMatches = findViewById(R.id.tvTotalMatches);

        // Progress bar
        progressBar = findViewById(R.id.progressBar);

        // Cards
        cardOverview = findViewById(R.id.cardOverview);
        cardOrganDemand = findViewById(R.id.cardOrganDemand);
        cardOrganSupply = findViewById(R.id.cardOrganSupply);

        // Charts
        pieChartOrganDemand = findViewById(R.id.pieChartOrganDemand);
        pieChartOrganSupply = findViewById(R.id.pieChartOrganSupply);
        barChartUserRegistration = findViewById(R.id.barChartUserRegistration);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
    }

    private void initializeCharts() {
        // Configure Pie Charts
        setupPieChart(pieChartOrganDemand, "Organ Demand Distribution");
        setupPieChart(pieChartOrganSupply, "Organ Supply Distribution");

        // Configure Bar Chart
        setupBarChart();
    }

    private void setupPieChart(PieChart pieChart, String description) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void setupBarChart() {
        barChartUserRegistration.setDrawBarShadow(false);
        barChartUserRegistration.setDrawValueAboveBar(true);
        barChartUserRegistration.getDescription().setEnabled(false);
        barChartUserRegistration.setMaxVisibleValueCount(60);
        barChartUserRegistration.setPinchZoom(false);
        barChartUserRegistration.setDrawGridBackground(false);

        XAxis xAxis = barChartUserRegistration.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        YAxis leftAxis = barChartUserRegistration.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = barChartUserRegistration.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f);

        Legend legend = barChartUserRegistration.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(9f);
        legend.setTextSize(11f);
        legend.setXEntrySpace(4f);
    }

    private void loadAnalyticsData() {
        progressBar.setVisibility(View.VISIBLE);

        // Load all data concurrently
        loadHospitalData();
        loadDonorData();
        loadRecipientData();
        loadMatchesData(); // Load actual completed matches
        loadMonthlyRegistrationData();
    }

    private void loadHospitalData() {
        db.collection("hospitals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalHospitals = queryDocumentSnapshots.size();
                    if (totalHospitals == 0) {
                        // Show sample data for demo purposes
                        totalHospitals = 12;
                    }
                    updateOverviewStats();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading hospital data", Toast.LENGTH_SHORT).show();
                    // Show sample data on error
                    totalHospitals = 8;
                    updateOverviewStats();
                });
    }

    private void loadDonorData() {
        db.collection("donors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalDonors = queryDocumentSnapshots.size();
                    activeDonors = 0; // Reset count

                    if (queryDocumentSnapshots.isEmpty()) {
                        // Create sample organ supply data for demo
                        createSampleOrganData();
                    } else {
                        // Process organ supply data and count active donors
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String organType = (String) document.get(FieldPath.of("16]Organs_to_donate"));
                            if (organType != null && !organType.isEmpty()) {
                                // Check if donor has available organs (not N/A or empty)
                                if (!organType.equals("N/A") && !organType.trim().isEmpty()) {
                                    activeDonors++; // Count as active donor
                                }
                                
                                // Split by comma since donors can select multiple organs
                                String[] organs = organType.split(",");
                                for (String organ : organs) {
                                    String trimmedOrgan = organ.trim();
                                    if (!trimmedOrgan.isEmpty()) {
                                        organSupplyMap.put(trimmedOrgan, organSupplyMap.getOrDefault(trimmedOrgan, 0) + 1);
                                    }
                                }
                            }
                        }
                    }
                    updateOverviewStats();
                    updateOrganCharts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading donor data", Toast.LENGTH_SHORT).show();
                    // Show sample data on error
                    totalDonors = 8;
                    activeDonors = 6;
                    createSampleOrganData();
                    updateOverviewStats();
                    updateOrganCharts();
                });
    }

    private void loadRecipientData() {
        db.collection("Recepients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalRecipients = queryDocumentSnapshots.size();
                    activeRecipients = 0; // Reset count

                    if (queryDocumentSnapshots.isEmpty()) {
                        // Create sample organ demand data for demo
                        createSampleOrganDemandData();
                    } else {
                        // Process organ demand data and count active recipients
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Check if recipient has completed organ request (not completed)
                            String status = document.getString("status");
                            if (status == null || !status.equals("completed")) {
                                activeRecipients++; // Count as active recipient
                            }
                            
                            String organNeeded = (String) document.get(FieldPath.of("16]Organs_to_donate"));
                            if (organNeeded != null && !organNeeded.isEmpty()) {
                                // Split by comma since recipients can select multiple organs
                                String[] organs = organNeeded.split(",");
                                for (String organ : organs) {
                                    String trimmedOrgan = organ.trim();
                                    if (!trimmedOrgan.isEmpty()) {
                                        organDemandMap.put(trimmedOrgan, organDemandMap.getOrDefault(trimmedOrgan, 0) + 1);
                                    }
                                }
                            }
                        }
                    }

                    updateOverviewStats();
                    updateOrganCharts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading recipient data", Toast.LENGTH_SHORT).show();
                    // Show sample data on error
                    totalRecipients = 21;
                    activeRecipients = 18;
                    createSampleOrganDemandData();
                    updateOverviewStats();
                    updateOrganCharts();
                });
    }

    private void loadMatchesData() {
        db.collection("organ_requests")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalMatches = queryDocumentSnapshots.size();
                    updateOverviewStats();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading matches data", Toast.LENGTH_SHORT).show();
                    // Show sample data on error
                    totalMatches = 15;
                    updateOverviewStats();
                });
    }

    private void createSampleOrganData() {
        // Sample organ supply data
        organSupplyMap.put("Kidney", 18);
        organSupplyMap.put("Liver", 8);
        organSupplyMap.put("Heart", 5);
        organSupplyMap.put("Lung", 6);
        organSupplyMap.put("Pancreas", 3);
        organSupplyMap.put("Cornea", 12);
        organSupplyMap.put("Bone Marrow", 7);
    }

    private void createSampleOrganDemandData() {
        // Sample organ demand data
        organDemandMap.put("Kidney", 25);
        organDemandMap.put("Liver", 12);
        organDemandMap.put("Heart", 8);
        organDemandMap.put("Lung", 9);
        organDemandMap.put("Pancreas", 4);
        organDemandMap.put("Cornea", 15);
        organDemandMap.put("Bone Marrow", 6);
    }

    private void loadMonthlyRegistrationData() {
        // Load monthly data from all collections
        loadMonthlyDataFromCollection("donors", "19]registration_timestamp");
        loadMonthlyDataFromCollection("hospitals", "13]registration_timestamp");
        loadMonthlyDataFromCollection("Recepients", "17]registration_timestamp");
    }

    private void loadMonthlyDataFromCollection(String collectionName, String timestampField) {
        db.collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Use get() with FieldPath for field names with special characters
                        Object timestampObj = document.get(FieldPath.of(timestampField));
                        if (timestampObj instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) timestampObj;
                            if (timestamp != null) {
                                // Convert timestamp to month-year format (e.g., "Mar 2026")
                                java.util.Date date = timestamp.toDate();
                                java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());
                                String monthYear = monthFormat.format(date);

                                monthlyRegistrationsMap.put(monthYear, monthlyRegistrationsMap.getOrDefault(monthYear, 0) + 1);
                            }
                        }
                    }
                    updateMonthlyChart();
                })
                .addOnFailureListener(e -> {
                    // Failure handled in updateMonthlyChart
                    updateMonthlyChart();
                });
    }

    private void updateOverviewStats() {
        tvTotalHospitals.setText(String.valueOf(totalHospitals));
        tvTotalDonors.setText(String.valueOf(totalDonors));
        tvTotalRecipients.setText(String.valueOf(totalRecipients));

        // Calculate active users (real-time data)
        tvActiveDonors.setText(String.valueOf(activeDonors));
        tvActiveRecipients.setText(String.valueOf(activeRecipients));
        tvTotalMatches.setText(String.valueOf(totalMatches));

        progressBar.setVisibility(View.GONE);
    }

    private void updateOrganCharts() {
        // Update Organ Demand Pie Chart
        List<PieEntry> demandEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : organDemandMap.entrySet()) {
            demandEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (!demandEntries.isEmpty()) {
            PieDataSet demandDataSet = new PieDataSet(demandEntries, "Organ Demand");
            demandDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            demandDataSet.setValueTextSize(12f);
            demandDataSet.setValueTextColor(Color.WHITE);

            PieData demandData = new PieData(demandDataSet);
            demandData.setValueFormatter(new PercentFormatter(pieChartOrganDemand));
            pieChartOrganDemand.setData(demandData);
            pieChartOrganDemand.invalidate();
        }

        // Update Organ Supply Pie Chart
        List<PieEntry> supplyEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : organSupplyMap.entrySet()) {
            supplyEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (!supplyEntries.isEmpty()) {
            PieDataSet supplyDataSet = new PieDataSet(supplyEntries, "Organ Supply");
            supplyDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            supplyDataSet.setValueTextSize(12f);
            supplyDataSet.setValueTextColor(Color.WHITE);

            PieData supplyData = new PieData(supplyDataSet);
            supplyData.setValueFormatter(new PercentFormatter(pieChartOrganSupply));
            pieChartOrganSupply.setData(supplyData);
            pieChartOrganSupply.invalidate();
        }
    }

    private void updateMonthlyChart() {
        // Clear existing data
        months.clear();
        monthlyRegistrations.clear();

        // Sort months chronologically and prepare data for chart
        List<String> sortedMonths = new ArrayList<>(monthlyRegistrationsMap.keySet());
        sortedMonths.sort((a, b) -> {
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());
                return format.parse(a).compareTo(format.parse(b));
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });

        // Take last 12 months or all available data
        int startIndex = Math.max(0, sortedMonths.size() - 12);
        for (int i = startIndex; i < sortedMonths.size(); i++) {
            String month = sortedMonths.get(i);
            months.add(month);
            monthlyRegistrations.add(monthlyRegistrationsMap.get(month));
        }

        // If no real data, show sample data
        if (months.isEmpty()) {
            createSampleMonthlyData();
        } else {
            updateBarChart();
        }
    }

    private void createSampleMonthlyData() {
        // Sample data - you can replace this with real monthly data from Firestore
        months.clear();
        monthlyRegistrations.clear();

        months.add("Jan"); months.add("Feb"); months.add("Mar"); months.add("Apr");
        months.add("May"); months.add("Jun"); months.add("Jul"); months.add("Aug");
        months.add("Sep"); months.add("Oct"); months.add("Nov"); months.add("Dec");

        // Sample registration numbers per month
        monthlyRegistrations.add(15); monthlyRegistrations.add(22); monthlyRegistrations.add(18);
        monthlyRegistrations.add(25); monthlyRegistrations.add(30); monthlyRegistrations.add(28);
        monthlyRegistrations.add(35); monthlyRegistrations.add(40); monthlyRegistrations.add(38);
        monthlyRegistrations.add(45); monthlyRegistrations.add(42); monthlyRegistrations.add(50);

        updateBarChart();
    }

    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < monthlyRegistrations.size(); i++) {
            entries.add(new BarEntry(i, monthlyRegistrations.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Registrations");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChartUserRegistration.setData(barData);
        barChartUserRegistration.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        barChartUserRegistration.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
