
package com.example.lifelink;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private Spinner spBloodGroup;
    private Button btnRegister;
    private TextView tvBack;

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

        if (name.isEmpty()) {
            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
            Toast.makeText(this,
                    "Please select a blood group",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must contain at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        Toast.makeText(this,
                "Form validated! Account creation will be connected next.",
                Toast.LENGTH_LONG).show();
    }
}
