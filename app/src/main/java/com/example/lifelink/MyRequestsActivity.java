package com.example.lifelink;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MyRequestsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressBar progressMyRequests;
    private TextView tvRequestsStatus;
    private TextView btnBack;
    private Button btnRefreshRequests;
    private LinearLayout layoutMyRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressMyRequests = findViewById(R.id.progressMyRequests);
        tvRequestsStatus = findViewById(R.id.tvRequestsStatus);
        btnBack = findViewById(R.id.btnBack);
        btnRefreshRequests = findViewById(R.id.btnRefreshRequests);
        layoutMyRequests = findViewById(R.id.layoutMyRequests);

        btnBack.setOnClickListener(v -> finish());
        btnRefreshRequests.setOnClickListener(v -> loadRequests());

        loadRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutMyRequests != null) {
            loadRequests();
        }
    }

    private void loadRequests() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in to view your requests.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);
        layoutMyRequests.removeAllViews();
        tvRequestsStatus.setText("Loading your requests...");

        db.collection("bloodRequests")
                .whereEqualTo("requesterId", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    layoutMyRequests.removeAllViews();

                    if (snapshot.isEmpty()) {
                        tvRequestsStatus.setText(
                                "You haven't submitted any blood requests yet."
                        );
                    } else {
                        tvRequestsStatus.setText(
                                snapshot.size() + (snapshot.size() == 1
                                        ? " request found"
                                        : " requests found")
                        );

                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            addRequestCard(document);
                        }
                    }

                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    setLoading(false);
                    tvRequestsStatus.setText(
                            "Could not load requests. Please try again."
                    );

                    Toast.makeText(
                            MyRequestsActivity.this,
                            "Error: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addRequestCard(DocumentSnapshot document) {
        String requestId = document.getId();

        String patientName = document.getString("patientName");
        String bloodGroup = document.getString("bloodGroup");
        String hospitalName = document.getString("hospitalName");
        String district = document.getString("district");
        String urgency = document.getString("urgency");
        String status = document.getString("status");

        Long unitsValue = document.getLong("unitsRequired");
        long units = unitsValue == null ? 0 : unitsValue;

        if (patientName == null) patientName = "Patient";
        if (bloodGroup == null) bloodGroup = "Not specified";
        if (hospitalName == null) hospitalName = "Not specified";
        if (district == null) district = "Not specified";
        if (urgency == null) urgency = "Normal";
        if (status == null) status = "OPEN";

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), Color.rgb(235, 220, 220));
        card.setBackground(background);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(patientName);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(45, 45, 45));
        card.addView(title);

        TextView details = new TextView(this);
        details.setText(
                "Blood Group: " + bloodGroup
                        + "\nUnits Required: " + units
                        + "\nHospital: " + hospitalName
                        + "\nDistrict: " + district
                        + "\nUrgency: " + urgency
        );
        details.setTextSize(14);
        details.setTextColor(Color.rgb(90, 90, 90));

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        detailsParams.topMargin = dp(8);
        card.addView(details, detailsParams);

        TextView statusView = new TextView(this);
        statusView.setText("Status: " + status);
        statusView.setTextSize(14);
        statusView.setTypeface(null, Typeface.BOLD);

        if ("OPEN".equalsIgnoreCase(status)) {
            statusView.setTextColor(Color.rgb(30, 125, 65));
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            statusView.setTextColor(Color.rgb(70, 80, 180));
        } else {
            statusView.setTextColor(Color.rgb(130, 90, 90));
        }

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(10);
        card.addView(statusView, statusParams);

        if ("OPEN".equalsIgnoreCase(status)) {
            Button cancelButton = new Button(this);
            cancelButton.setText("Cancel Request");
            cancelButton.setAllCaps(false);
            cancelButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.rgb(190, 25, 45)
                    )
            );
            cancelButton.setTextColor(Color.WHITE);

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(46)
                    );

            buttonParams.topMargin = dp(10);
            card.addView(cancelButton, buttonParams);

            cancelButton.setOnClickListener(v ->
                    confirmCancelRequest(requestId, cancelButton));
        }

        layoutMyRequests.addView(card);
    }

    private void confirmCancelRequest(String requestId, Button cancelButton) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Blood Request?")
                .setMessage("Are you sure you no longer need this request?")
                .setNegativeButton("Keep Request", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Cancel Request", (dialog, which) ->
                        cancelRequest(requestId, cancelButton))
                .show();
    }

    private void cancelRequest(String requestId, Button cancelButton) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in again.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cancelButton.setEnabled(false);
        cancelButton.setText("Cancelling...");

        db.collection("bloodRequests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()
                            || !user.getUid().equals(
                            document.getString("requesterId"))) {
                        cancelButton.setEnabled(true);
                        cancelButton.setText("Cancel Request");

                        Toast.makeText(
                                this,
                                "Request not found or access denied.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (!"OPEN".equalsIgnoreCase(document.getString("status"))) {
                        cancelButton.setEnabled(true);
                        cancelButton.setText("Cancel Request");

                        Toast.makeText(
                                this,
                                "Only open requests can be cancelled.",
                                Toast.LENGTH_LONG
                        ).show();
                        loadRequests();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "CANCELLED");
                    updates.put("updatedAt", FieldValue.serverTimestamp());

                    db.collection("bloodRequests")
                            .document(requestId)
                            .update(updates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Blood request cancelled.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadRequests();
                            })
                            .addOnFailureListener(e -> {
                                cancelButton.setEnabled(true);
                                cancelButton.setText("Cancel Request");

                                Toast.makeText(
                                        this,
                                        "Could not cancel request: "
                                                + e.getLocalizedMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    cancelButton.setEnabled(true);
                    cancelButton.setText("Cancel Request");

                    Toast.makeText(
                            this,
                            "Could not verify request ownership.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void setLoading(boolean loading) {
        progressMyRequests.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnRefreshRequests.setEnabled(!loading);

        btnRefreshRequests.setText(
                loading ? "Loading..." : "Refresh Requests"
        );
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}
