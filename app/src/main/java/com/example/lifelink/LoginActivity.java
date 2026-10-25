package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvBack, tvRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvBack = findViewById(R.id.tvBack);
        tvRegister = findViewById(R.id.tvRegister);

        tvBack.setOnClickListener(v -> finish());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    HomeActivity.class
                            );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();

                        } else {
                            showLoginError(
                                    "Unable to retrieve your account."
                            );
                        }

                    } else {
                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Login failed. Please try again.";

                        showLoginError(errorMessage);
                    }
                });
    }

    private void showLoginError(String message) {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");

        Toast.makeText(
                LoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}