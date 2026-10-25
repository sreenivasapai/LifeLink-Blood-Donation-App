package com.example.lifelink;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RequestBloodActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etPatientName;
    private EditText etUnitsRequired;
    private EditText etHospitalName;
    private EditText etRequestDistrict;
    private EditText etHospitalLocation;
    private EditText etContactPerson;
    private EditText etContactPhone;

    private Spinner spRequestBloodGroup;
    private Spinner spUrgency;

    private Button btnSubmitRequest;
    private ProgressBar progressRequest;
    private TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etPatientName = findViewById(R.id.etPatientName);
        etUnitsRequired = findViewById(R.id.etUnitsRequired);
        etHospitalName = findViewById(R.id.etHospitalName);
        etRequestDistrict = findViewById(R.id.etRequestDistrict);
        etHospitalLocation = findViewById(R.id.etHospitalLocation);
        etContactPerson = findViewById(R.id.etContactPerson);
        etContactPhone = findViewById(R.id.etContactPhone);

        spRequestBloodGroup = findViewById(R.id.spRequestBloodGroup);
        spUrgency = findViewById(R.id.spUrgency);

        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        progressRequest = findViewById(R.id.progressRequest);
        btnBack = findViewById(R.id.btnBack);

        String[] bloodGroups = {
                "Select Blood Group",
                "A+", "A-", "B+", "B-",
                "AB+", "AB-", "O+", "O-"
        };

        String[] urgencyOptions = {
                "Select Urgency",
                "Normal",
                "Urgent"
        };

        setupSpinner(spRequestBloodGroup, bloodGroups);
        setupSpinner(spUrgency, urgencyOptions);

        btnBack.setOnClickListener(v -> finish());

        btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    private void submitRequest() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    this,
                    "Please log in to submit a blood request.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        String patientName = etPatientName.getText().toString().trim();
        String unitsText = etUnitsRequired.getText().toString().trim();
        String hospitalName = etHospitalName.getText().toString().trim();
        String district = etRequestDistrict.getText().toString().trim();
        String hospitalLocation = etHospitalLocation.getText().toString().trim();
        String contactPerson = etContactPerson.getText().toString().trim();
        String contactPhone = etContactPhone.getText().toString().trim();

        String bloodGroup = spRequestBloodGroup.getSelectedItem().toString();
        String urgency = spUrgency.getSelectedItem().toString();

        if (TextUtils.isEmpty(patientName)) {
            etPatientName.setError("Enter the patient's name");
            etPatientName.requestFocus();
            return;
        }

        if (bloodGroup.equals("Select Blood Group")) {
            Toast.makeText(this, "Select the required blood group.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(unitsText)) {
            etUnitsRequired.setError("Enter the number of units");
            etUnitsRequired.requestFocus();
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsText);
        } catch (NumberFormatException e) {
            etUnitsRequired.setError("Enter a valid number");
            etUnitsRequired.requestFocus();
            return;
        }

        if (units < 1 || units > 20) {
            etUnitsRequired.setError("Enter a value between 1 and 20");
            etUnitsRequired.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(hospitalName)) {
            etHospitalName.setError("Enter the hospital name");
            etHospitalName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(district)) {
            etRequestDistrict.setError("Enter the district");
            etRequestDistrict.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(hospitalLocation)) {
            etHospitalLocation.setError("Enter the hospital address");
            etHospitalLocation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contactPerson)) {
            etContactPerson.setError("Enter a contact person's name");
            etContactPerson.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contactPhone)) {
            etContactPhone.setError("Enter the contact phone number");
            etContactPhone.requestFocus();
            return;
        }

        String digitsOnly = contactPhone.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            etContactPhone.setError("Enter a valid phone number");
            etContactPhone.requestFocus();
            return;
        }

        if (urgency.equals("Select Urgency")) {
            Toast.makeText(this, "Select the urgency level.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, Object> request = new HashMap<>();
        request.put("requesterId", currentUser.getUid());
        request.put("patientName", patientName);
        request.put("bloodGroup", bloodGroup);
        request.put("unitsRequired", units);
        request.put("hospitalName", hospitalName);
        request.put("district", district);
        request.put("hospitalLocation", hospitalLocation);
        request.put("contactPerson", contactPerson);
        request.put("contactPhone", contactPhone);
        request.put("urgency", urgency);
        request.put("status", "OPEN");
        request.put("createdAt", FieldValue.serverTimestamp());
        request.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("bloodRequests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);

                    Toast.makeText(
                            RequestBloodActivity.this,
                            "Blood request submitted successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);

                    Toast.makeText(
                            RequestBloodActivity.this,
                            "Could not submit request: "
                                    + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void setLoading(boolean loading) {
        progressRequest.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnSubmitRequest.setEnabled(!loading);

        btnSubmitRequest.setText(
                loading ? "Submitting..." : "Submit Blood Request"
        );
    }
}
