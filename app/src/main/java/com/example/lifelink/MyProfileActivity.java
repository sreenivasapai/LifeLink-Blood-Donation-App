package com.example.lifelink;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MyProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etName, etEmail, etPhone;
    private Spinner spinnerBloodGroup;
    private TextView tvDonorStatus, btnBack;
    private Button btnSave;
    private ProgressBar progressBar;

    private boolean isLoadingProfile = false;

    private final String[] bloodGroups = {
            "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etProfileName);
        etEmail = findViewById(R.id.etProfileEmail);
        etPhone = findViewById(R.id.etProfilePhone);
        spinnerBloodGroup = findViewById(R.id.spinnerProfileBloodGroup);
        tvDonorStatus = findViewById(R.id.tvProfileDonorStatus);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressProfile);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bloodGroups
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerBloodGroup.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (!document.exists()) {
                        setLoading(false);
                        Toast.makeText(
                                this,
                                "Profile not found in Firestore.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    etName.setText(document.getString("name"));
                    etEmail.setText(user.getEmail());
                    etPhone.setText(document.getString("phone"));

                    String bloodGroup = document.getString("bloodGroup");
                    if (bloodGroup != null) {
                        for (int i = 0; i < bloodGroups.length; i++) {
                            if (bloodGroups[i].equals(bloodGroup)) {
                                spinnerBloodGroup.setSelection(i);
                                break;
                            }
                        }
                    }

                    Boolean isDonor = document.getBoolean("isDonor");
                    tvDonorStatus.setText(
                            Boolean.TRUE.equals(isDonor)
                                    ? "Donor status: Registered"
                                    : "Donor status: Not registered"
                    );

                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;

                    setLoading(false);
                    Toast.makeText(
                            this,
                            "Could not load profile: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }

        if (!name.matches("[\\p{L} .'-]{2,60}")) {
            etName.setError("Enter a valid name");
            etName.requestFocus();
            return;
        }

        if (!phone.matches("[0-9+() -]{7,15}")) {
            etPhone.setError("Enter a valid phone number");
            etPhone.requestFocus();
            return;
        }

        setLoading(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("bloodGroup", bloodGroup);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (isFinishing() || isDestroyed()) return;

                    setLoading(false);
                    Toast.makeText(
                            this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;

                    setLoading(false);
                    Toast.makeText(
                            this,
                            "Could not update profile: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
        btnSave.setText(loading ? "Please wait..." : "Save Changes");
    }
}
