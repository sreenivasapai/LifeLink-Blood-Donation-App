package com.example.lifelink;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FindDonorsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Spinner spSearchBloodGroup;
    private EditText etSearchDistrict;
    private CheckBox cbAvailableOnly;
    private Button btnSearchDonors;
    private ProgressBar progressDonors;
    private TextView tvSearchStatus;
    private TextView btnBack;
    private LinearLayout layoutDonorResults;

    private final List<Donor> allDonors = new ArrayList<>();

    private static final String[] BLOOD_GROUPS = {
            "All Blood Groups",
            "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_donors);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        spSearchBloodGroup = findViewById(R.id.spSearchBloodGroup);
        etSearchDistrict = findViewById(R.id.etSearchDistrict);
        cbAvailableOnly = findViewById(R.id.cbAvailableOnly);
        btnSearchDonors = findViewById(R.id.btnSearchDonors);
        progressDonors = findViewById(R.id.progressDonors);
        tvSearchStatus = findViewById(R.id.tvSearchStatus);
        btnBack = findViewById(R.id.btnBack);
        layoutDonorResults = findViewById(R.id.layoutDonorResults);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                BLOOD_GROUPS
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spSearchBloodGroup.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSearchDonors.setOnClickListener(v -> searchDonors());

        // Load and display donors when the screen opens.
        searchDonors();
    }

    private void searchDonors() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);
        layoutDonorResults.removeAllViews();
        tvSearchStatus.setText("Searching donors...");

        String selectedGroup = spSearchBloodGroup.getSelectedItem().toString();
        String districtInput = etSearchDistrict.getText().toString().trim();
        boolean availableOnly = cbAvailableOnly.isChecked();

        db.collection("donorProfiles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    allDonors.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String userId = document.getString("userId");
                        String name = document.getString("name");
                        String bloodGroup = document.getString("bloodGroup");
                        String district = document.getString("district");
                        String location = document.getString("location");
                        Boolean availableValue = document.getBoolean("available");

                        boolean available = Boolean.TRUE.equals(availableValue);

                        if (TextUtils.isEmpty(userId)
                                || TextUtils.isEmpty(name)
                                || TextUtils.isEmpty(bloodGroup)) {
                            continue;
                        }

                        // Apply the selected filters.
                        if (!selectedGroup.equals("All Blood Groups")
                                && !selectedGroup.equalsIgnoreCase(bloodGroup)) {
                            continue;
                        }

                        if (!districtInput.isEmpty()
                                && (district == null
                                || !district.toLowerCase(Locale.ROOT)
                                .contains(districtInput.toLowerCase(Locale.ROOT)))) {
                            continue;
                        }

                        if (availableOnly && !available) {
                            continue;
                        }

                        allDonors.add(new Donor(
                                userId,
                                name,
                                bloodGroup,
                                district == null ? "" : district,
                                location == null ? "" : location,
                                available
                        ));
                    }

                    displayDonors(allDonors);
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    setLoading(false);
                    tvSearchStatus.setText(
                            "Unable to load donors. Please try again."
                    );

                    Toast.makeText(
                            FindDonorsActivity.this,
                            "Search failed. Check your connection and Firestore rules.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void displayDonors(List<Donor> donors) {
        layoutDonorResults.removeAllViews();

        if (donors.isEmpty()) {
            tvSearchStatus.setText(
                    "No matching donors found. Try changing your filters."
            );
            return;
        }

        tvSearchStatus.setText(
                donors.size() + (donors.size() == 1
                        ? " donor found"
                        : " donors found")
        );

        for (Donor donor : donors) {
            addDonorCard(donor);
        }
    }

    private void addDonorCard(Donor donor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), Color.rgb(235, 220, 220));
        card.setBackground(background);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        TextView nameView = new TextView(this);
        nameView.setText(donor.name);
        nameView.setTextSize(18);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setTextColor(Color.rgb(45, 45, 45));
        card.addView(nameView);

        TextView groupView = new TextView(this);
        groupView.setText("Blood Group: " + donor.bloodGroup);
        groupView.setTextSize(15);
        groupView.setTypeface(null, Typeface.BOLD);
        groupView.setTextColor(Color.rgb(190, 25, 45));

        LinearLayout.LayoutParams groupParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        groupParams.topMargin = dp(6);
        card.addView(groupView, groupParams);

        TextView locationView = new TextView(this);
        String districtText = donor.district.isEmpty()
                ? "District not provided" : donor.district;
        String localityText = donor.location.isEmpty()
                ? "Locality not provided" : donor.location;

        locationView.setText(
                "District: " + districtText + "\nLocality: " + localityText
        );
        locationView.setTextSize(14);
        locationView.setTextColor(Color.rgb(90, 90, 90));

        LinearLayout.LayoutParams locationParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        locationParams.topMargin = dp(8);
        card.addView(locationView, locationParams);

        TextView availabilityView = new TextView(this);
        availabilityView.setText(
                donor.available
                        ? "● Currently available"
                        : "● Not currently available"
        );
        availabilityView.setTextSize(13);
        availabilityView.setTextColor(
                donor.available
                        ? Color.rgb(30, 125, 65)
                        : Color.rgb(130, 90, 90)
        );

        LinearLayout.LayoutParams availabilityParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        availabilityParams.topMargin = dp(8);
        card.addView(availabilityView, availabilityParams);

        // Do not display or fetch private phone numbers here.
        layoutDonorResults.addView(card);
    }

    private void setLoading(boolean loading) {
        progressDonors.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        btnSearchDonors.setEnabled(!loading);
        btnSearchDonors.setText(
                loading ? "Searching..." : "Search Donors"
        );
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private static class Donor {
        final String userId;
        final String name;
        final String bloodGroup;
        final String district;
        final String location;
        final boolean available;

        Donor(String userId, String name, String bloodGroup,
              String district, String location, boolean available) {
            this.userId = userId;
            this.name = name;
            this.bloodGroup = bloodGroup;
            this.district = district;
            this.location = location;
            this.available = available;
        }
    }
}
