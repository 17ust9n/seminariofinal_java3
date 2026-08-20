package com.example.seminariofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class OnboardingActivity extends AppCompatActivity {

    private EditText etPhone;
    private Button btnSecNormal;
    private Button btnSecBlindado;
    private Button btnEnter;
    private TextView tvError;

    // Nivel de seguridad actual: 0 = Normal, 1 = Blindado
    private int selectedSecurityLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setSec(0); // Estado por defecto: Normal
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        btnSecNormal = findViewById(R.id.btnSecNormal);
        btnSecBlindado = findViewById(R.id.btnSecBlindado);
        btnEnter = findViewById(R.id.btnEnter);
        tvError = findViewById(R.id.tvError);
    }

    private void setupListeners() {
        // Eventos para cambiar el modo de seguridad
        btnSecNormal.setOnClickListener(v -> setSec(0));
        btnSecBlindado.setOnClickListener(v -> setSec(1));

        // Evento al presionar el botón "Entrar"
        btnEnter.setOnClickListener(v -> saveMe());

        // Evento al presionar Enter/Hecho en el teclado del celular
        etPhone.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED) {
                saveMe();
                return true;
            }
            return false;
        });
    }

    // Equivalente a setSec(sec) en tu JS
    private void setSec(int level) {
        selectedSecurityLevel = level;

        if (level == 0) {
            // Normal Seleccionado
            btnSecNormal.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
            btnSecNormal.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            btnSecBlindado.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            btnSecBlindado.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        } else {
            // Blindado Seleccionado
            btnSecBlindado.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
            btnSecBlindado.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            btnSecNormal.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            btnSecNormal.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    // Equivalente a saveMe() en tu JS
    private void saveMe() {
        String phoneNumber = etPhone.getText().toString().trim();

        // Validar número
        if (TextUtils.isEmpty(phoneNumber)) {
            showError("Ingresa un número válido con código de país");
            return;
        }

        if (phoneNumber.length() < 8) {
            showError("El número ingresado es muy corto");
            return;
        }

        // Limpiar errores si la validación pasa
        tvError.setVisibility(View.GONE);

        // Guardar sesión (ME y SECURE) en SharedPreferences
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("user_phone", phoneNumber);           // Representa a ME
        editor.putInt("security_level", selectedSecurityLevel); // Representa a SECURE
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}