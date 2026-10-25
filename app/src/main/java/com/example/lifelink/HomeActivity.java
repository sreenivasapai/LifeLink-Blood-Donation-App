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
    private Button btnFindDonors, btnRequestBlood, btnBecomeDonor;
    private Button btnMyRequests, btnProfile, btnLogout;

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

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        tvWelcome.setText(
                                "Welcome, " +
                                        (name != null ? name : "LifeLink User") +
                                        "!"
                        );
                    } else {
                        tvWelcome.setText("Welcome to LifeLink!");
                    }
                })
                .addOnFailureListener(e ->
                        tvWelcome.setText("Welcome to LifeLink!"));

        btnFindDonors.setOnClickListener(v ->
                Toast.makeText(this,
                        "Find Donors is coming next",
                        Toast.LENGTH_SHORT).show());

        btnRequestBlood.setOnClickListener(v ->
                Toast.makeText(this,
                        "Blood Requests are coming next",
                        Toast.LENGTH_SHORT).show());

        btnBecomeDonor.setOnClickListener(v ->
                Toast.makeText(this,
                        "Donor registration is coming next",
                        Toast.LENGTH_SHORT).show());

        btnMyRequests.setOnClickListener(v ->
                Toast.makeText(this,
                        "My Requests is coming next",
                        Toast.LENGTH_SHORT).show());

        btnProfile.setOnClickListener(v ->
                Toast.makeText(this,
                        "My Profile is coming next",
                        Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            openWelcomeScreen();
        });
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