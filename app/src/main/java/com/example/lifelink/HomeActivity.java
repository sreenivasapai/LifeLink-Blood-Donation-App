package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvWelcome;
    private Button btnFindDonors;
    private Button btnRequestBlood;
    private Button btnBecomeDonor;
    private Button btnMyRequests;
    private Button btnProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        btnFindDonors = findViewById(R.id.btnFindDonors);
        btnRequestBlood = findViewById(R.id.btnRequestBlood);
        btnBecomeDonor = findViewById(R.id.btnBecomeDonor);
        btnMyRequests = findViewById(R.id.btnMyRequests);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            openWelcomeScreen();
            return;
        }

        // Display the user's name from Firestore.
        tvWelcome.setText("Welcome to LifeLink!");

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (!isFinishing() && !isDestroyed()) {
                        if (document.exists()) {
                            String name = document.getString("name");

                            if (name != null && !name.trim().isEmpty()) {
                                tvWelcome.setText("Welcome, " + name + "!");
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                HomeActivity.this,
                                "Could not load profile. Please try again later.",
                                Toast.LENGTH_SHORT
                        ).show()
                );

        btnFindDonors.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    FindDonorsActivity.class
            );
            startActivity(intent);
        });

        btnRequestBlood.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    RequestBloodActivity.class
            );
            startActivity(intent);
        });

        btnBecomeDonor.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BecomeDonorActivity.class);
            startActivity(intent);
        });

        btnMyRequests.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    MyRequestsActivity.class
            );
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    MyProfileActivity.class
            );
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            openWelcomeScreen();
        });
    }

    private void showComingSoon(String feature) {
        Toast.makeText(
                this,
                feature + " is coming soon!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void openWelcomeScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
}
