package com.example.seminariofinal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.utils.KeyPair;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbarProfile;
    private ShapeableImageView imgProfilePicture;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private Button btnEditProfile;
    private Button btnSettings;
    private Button btnLogout;
    private BottomNavigationView bottomNavigation;

    private LazySodiumAndroid sodium;
    private String myPublicKeyHex = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Uso de tu SodiumManager existente
        sodium = SodiumManager.getInstance();

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
        // 1. Datos generales de preferencias comunes
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);

        String userPhone = preferences.getString("user_phone", "Sin número");
        int secLevel = preferences.getInt("security_level", 0);
        String secLabel = (secLevel == 1) ? "Modo Blindado 🛡️" : "Modo Normal ⚡";

        tvUserName.setText(userPhone);

        // 2. Leer o generar par de claves Curve25519 con Libsodium en EncryptedSharedPreferences
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    this,
                    "starssenger_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            myPublicKeyHex = securePrefs.getString("public_key", null);
            String privateKeyHex = securePrefs.getString("private_key", null);

            // Generar par de claves con Libsodium si aún no existen
            if (myPublicKeyHex == null || privateKeyHex == null) {
                KeyPair keyPair = sodium.cryptoBoxKeypair();
                myPublicKeyHex = keyPair.getPublicKey().getAsHexString();
                privateKeyHex = keyPair.getSecretKey().getAsHexString();

                securePrefs.edit()
                        .putString("public_key", myPublicKeyHex)
                        .putString("private_key", privateKeyHex)
                        .apply();
            }

            // Formatear la vista previa de la clave pública
            String shortKey = (myPublicKeyHex.length() > 16)
                    ? myPublicKeyHex.substring(0, 8) + "..." + myPublicKeyHex.substring(myPublicKeyHex.length() - 8)
                    : myPublicKeyHex;

            String subtext = secLabel + "\nClave Pública (Toca para copiar):\n" + shortKey;
            tvUserEmail.setText(subtext);

        } catch (Exception e) {
            e.printStackTrace();
            tvUserEmail.setText(secLabel + "\nError al cargar identidad Libsodium");
        }
    }

    private void setupListeners() {
        // Permite al usuario copiar su clave pública al portapapeles
        tvUserEmail.setOnClickListener(v -> {
            if (myPublicKeyHex != null && !myPublicKeyHex.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Clave Pública Libsodium", myPublicKeyHex);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "¡Clave pública copiada al portapapeles!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
        // 1. Limpiar preferencias generales
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        // 2. Eliminar claves criptográficas guardadas
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    this,
                    "starssenger_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            securePrefs.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Redirigir al Onboarding
        Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}