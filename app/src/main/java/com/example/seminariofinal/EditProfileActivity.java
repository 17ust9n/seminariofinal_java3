package com.example.seminariofinal; // <-- Debe decir EXACTAMENTE esto

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        etUserPhone = findViewById(R.id.etUserPhone);

        // Cargar número actual
        SharedPreferences prefs = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        etUserPhone.setText(prefs.getString("user_phone", ""));

        findViewById(R.id.btnSavePhone).setOnClickListener(v -> savePhone());
    }

    private void savePhone() {
        String phone = etUserPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            etUserPhone.setError("Ingresa un número válido");
            return;
        }

        getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("user_phone", phone)
                .apply();

        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
        finish();
    }
}