package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            openDashboard();
            return;
        }

        setContentView(R.layout.activity_main);

        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnLogin = findViewById(R.id.btnLogin);

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
        });
    }

    private void openDashboard() {
        Intent intent = new Intent(
                MainActivity.this,
                HomeActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}