package com.example.seminariofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbarProfile;
    private ShapeableImageView imgProfilePicture;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private Button btnEditProfile;
    private Button btnSettings;
    private Button btnLogout;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        toolbarProfile = findViewById(R.id.toolbarProfile);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);

        String username = preferences.getString("user_name", "Usuario Starssenger");
        String email = preferences.getString("user_email", "correo@ejemplo.com");

        tvUserName.setText(username);
        tvUserEmail.setText(email);
    }

    private void setupListeners() {
        // Se quitó el listener de la flecha de navegación

        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Navegar a Editar Perfil", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Navegar a Ajustes", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> logoutUser());

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void logoutUser() {
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}