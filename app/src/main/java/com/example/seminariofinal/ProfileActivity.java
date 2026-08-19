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
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbarProfile;
    private ShapeableImageView imgProfilePicture;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private Button btnEditProfile;
    private Button btnSettings;
    private Button btnLogout;

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
    }

    private void loadUserData() {
        // Cargar los datos del objeto ME almacenados localmente
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);

        String username = preferences.getString("user_name", "Usuario Starssenger");
        String email = preferences.getString("user_email", "correo@ejemplo.com");

        tvUserName.setText(username);
        tvUserEmail.setText(email);

        // Aquí podrías usar una librería como Glide o Picasso para cargar la imagen si tienes una URL
        // Glide.with(this).load(photoUrl).into(imgProfilePicture);
    }

    private void setupListeners() {
        // Botón de flecha atrás en la Toolbar
        toolbarProfile.setNavigationOnClickListener(v -> finish());

        // Botón Editar Perfil
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Navegar a Editar Perfil", Toast.LENGTH_SHORT).show();
            // TODO: Iniciar EditProfileActivity
        });

        // Botón Ajustes
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Navegar a Ajustes", Toast.LENGTH_SHORT).show();
            // TODO: Iniciar SettingsActivity
        });

        // Botón Cerrar Sesión
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        // 1. Limpiar SharedPreferences (borra el estado de sesión / ME)
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // 2. Redirigir al usuario al Login / Onboarding
        Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
        // Limpiar la pila de actividades para que no se pueda volver atrás con el botón físico
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}