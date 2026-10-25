package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private Spinner spBloodGroup;
    private Button btnRegister;
    private TextView tvBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        spBloodGroup = findViewById(R.id.spBloodGroup);
        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String[] bloodGroups = {
                "Select blood group",
                "A+", "A-", "B+", "B-",
                "AB+", "AB-", "O+", "O-",
                "Prefer not to say"
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

        tvBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> validateForm());
    }

    private void validateForm() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String bloodGroup = spBloodGroup.getSelectedItem().toString();

        if (name.isEmpty()) {
            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (!phone.matches("[6-9][0-9]{9}")) {
            etPhone.setError("Enter a valid 10-digit Indian mobile number");
            etPhone.requestFocus();
            return;
        }

        if (spBloodGroup.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    this,
                    "Please select a blood group",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must contain at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        btnRegister.setEnabled(true);

                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Account creation failed";

                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (mAuth.getCurrentUser() == null) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(
                                this,
                                "Account created, but user session is unavailable",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("phone", phone);
                    user.put("bloodGroup", bloodGroup);
                    user.put("isDonor", false);
                    user.put("createdAt",
                            com.google.firebase.firestore.FieldValue.serverTimestamp());

                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                Intent intent = new Intent(
                                        RegisterActivity.this,
                                        HomeActivity.class
                                );

                                intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnRegister.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Account created, but profile saving failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                });
    }
}