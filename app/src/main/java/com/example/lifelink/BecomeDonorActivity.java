package com.example.lifelink;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BecomeDonorActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etDonorName;
    private EditText etDonorPhone;
    private EditText etDistrict;
    private EditText etDonorLocation;

    private Spinner spBloodGroup;
    private CheckBox cbAvailable;
    private Button btnSaveDonor;
    private ProgressBar progressDonor;
    private TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_donor);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etDonorName = findViewById(R.id.etDonorName);
        etDonorPhone = findViewById(R.id.etDonorPhone);
        etDistrict = findViewById(R.id.etDistrict);
        etDonorLocation = findViewById(R.id.etDonorLocation);

        spBloodGroup = findViewById(R.id.spBloodGroup);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnSaveDonor = findViewById(R.id.btnSaveDonor);
        progressDonor = findViewById(R.id.progressDonor);
        btnBack = findViewById(R.id.btnBack);

        String[] bloodGroups = {
                "Select Blood Group",
                "A+", "A-", "B+", "B-",
                "AB+", "AB-", "O+", "O-"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bloodGroups
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spBloodGroup.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    this,
                    "Please log in to register as a donor.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        // Pre-fill the name from the user's existing Firestore profile.
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");

                        if (name != null && !name.trim().isEmpty()
                                && etDonorName.getText().toString().trim().isEmpty()) {
                            etDonorName.setText(name);
                        }
                    }
                });

        // Load an existing donor profile when the user returns.
        loadExistingDonor(currentUser.getUid());

        btnSaveDonor.setOnClickListener(v -> saveDonorProfile());
    }

    private void loadExistingDonor(String uid) {
        db.collection("donorProfiles")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists() || isFinishing() || isDestroyed()) {
                        return;
                    }

                    String name = document.getString("name");
                    String bloodGroup = document.getString("bloodGroup");
                    String district = document.getString("district");
                    String location = document.getString("location");
                    Boolean available = document.getBoolean("available");

                    if (name != null) {
                        etDonorName.setText(name);
                    }

                    if (district != null) {
                        etDistrict.setText(district);
                    }

                    if (location != null) {
                        etDonorLocation.setText(location);
                    }

                    if (bloodGroup != null) {
                        for (int i = 0; i < spBloodGroup.getCount(); i++) {
                            if (spBloodGroup.getItemAtPosition(i)
                                    .toString().equals(bloodGroup)) {
                                spBloodGroup.setSelection(i);
                                break;
                            }
                        }
                    }

                    if (available != null) {
                        cbAvailable.setChecked(available);
                    }

                    btnSaveDonor.setText("Update Donor Profile");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Could not load your existing donor profile.",
                                Toast.LENGTH_SHORT
                        ).show()
                );

        // Phone number is stored separately from searchable donor details.
        db.collection("donorPrivate")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && !isFinishing() && !isDestroyed()) {
                        String phone = document.getString("phoneNumber");

                        if (phone != null) {
                            etDonorPhone.setText(phone);
                        }
                    }
                });
    }

    private void saveDonorProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = etDonorName.getText().toString().trim();
        String phone = etDonorPhone.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String location = etDonorLocation.getText().toString().trim();
        String bloodGroup = spBloodGroup.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etDonorName.setError("Enter your full name");
            etDonorName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etDonorPhone.setError("Enter your phone number");
            etDonorPhone.requestFocus();
            return;
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            etDonorPhone.setError("Enter a valid phone number");
            etDonorPhone.requestFocus();
            return;
        }

        if (bloodGroup.equals("Select Blood Group")) {
            Toast.makeText(this, "Please select your blood group.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(district)) {
            etDistrict.setError("Enter your district");
            etDistrict.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etDonorLocation.setError("Enter your city, town, or locality");
            etDonorLocation.requestFocus();
            return;
        }

        setLoading(true);

        String uid = currentUser.getUid();

        // Public/searchable details: do not store the phone number here.
        Map<String, Object> publicProfile = new HashMap<>();
        publicProfile.put("userId", uid);
        publicProfile.put("name", name);
        publicProfile.put("bloodGroup", bloodGroup);
        publicProfile.put("district", district);
        publicProfile.put("location", location);
        publicProfile.put("available", cbAvailable.isChecked());
        publicProfile.put("updatedAt", FieldValue.serverTimestamp());

        // Private contact information is stored in a separate document.
        Map<String, Object> privateProfile = new HashMap<>();
        privateProfile.put("userId", uid);
        privateProfile.put("phoneNumber", phone);
        privateProfile.put("updatedAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();

        batch.set(
                db.collection("donorProfiles").document(uid),
                publicProfile,
                SetOptions.merge()
        );

        batch.set(
                db.collection("donorPrivate").document(uid),
                privateProfile,
                SetOptions.merge()
        );

        batch.commit()
                .addOnSuccessListener(unused -> {
                    setLoading(false);

                    Toast.makeText(
                            BecomeDonorActivity.this,
                            "Donor profile saved successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);

                    Toast.makeText(
                            BecomeDonorActivity.this,
                            "Could not save donor profile: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void setLoading(boolean loading) {
        progressDonor.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveDonor.setEnabled(!loading);
        btnSaveDonor.setText(loading ? "Saving..." : "Save Donor Profile");
    }
}
