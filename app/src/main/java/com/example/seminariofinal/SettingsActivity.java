package com.example.seminariofinal; // <-- Debe decir EXACTAMENTE esto

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchSecurityLevel;
    private TextView tvFullPublicKey;
    private String fullPublicKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        switchSecurityLevel = findViewById(R.id.switchSecurityLevel);
        tvFullPublicKey = findViewById(R.id.tvFullPublicKey);

        loadSettings();

        // Guardar cambio de nivel de seguridad
        switchSecurityLevel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int level = isChecked ? 1 : 0;
            getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("security_level", level)
                    .apply();
        });

        // Copiar clave pública completa
        findViewById(R.id.btnCopyKey).setOnClickListener(v -> {
            if (!fullPublicKey.isEmpty()) {
                ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cb != null) {
                    cb.setPrimaryClip(ClipData.newPlainText("Clave Pública", fullPublicKey));
                    Toast.makeText(this, "Clave pública copiada 📋", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        int secLevel = prefs.getInt("security_level", 0);
        switchSecurityLevel.setChecked(secLevel == 1);

        new Thread(() -> {
            try {
                MasterKey mk = new MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
                SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                        this, "starssenger_secure_prefs", mk,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                fullPublicKey = securePrefs.getString("public_key", "No generada");
                runOnUiThread(() -> tvFullPublicKey.setText(fullPublicKey));
            } catch (Exception e) {
                runOnUiThread(() -> tvFullPublicKey.setText("Error al cargar la clave"));
            }
        }).start();
    }
}