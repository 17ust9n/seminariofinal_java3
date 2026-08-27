package com.example.seminariofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private EditText etPhone;
    private MaterialButton btnSecNormal;
    private MaterialButton btnSecBlindado;
    private Button btnEnter;
    private TextView tvError;

    private int selectedSecurityLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setSec(0);
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
        btnSecNormal.setOnClickListener(v -> setSec(0));
        btnSecBlindado.setOnClickListener(v -> setSec(1));
        btnEnter.setOnClickListener(v -> saveMe());

        etPhone.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                saveMe();
                return true;
            }
            return false;
        });
    }

    private void setSec(int level) {
        selectedSecurityLevel = level;

        int activeColor = ContextCompat.getColor(this, R.color.green_accent);
        int activeTextColor = ContextCompat.getColor(this, R.color.green_text);
        int inactiveColor = ContextCompat.getColor(this, R.color.navy_input);
        int inactiveTextColor = ContextCompat.getColor(this, R.color.white);
        int borderColor = ContextCompat.getColor(this, R.color.line_border);

        if (level == 0) {
            btnSecNormal.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            btnSecNormal.setTextColor(activeTextColor);
            btnSecNormal.setStrokeWidth(0);

            btnSecBlindado.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            btnSecBlindado.setTextColor(inactiveTextColor);
            btnSecBlindado.setStrokeColor(ColorStateList.valueOf(borderColor));
            btnSecBlindado.setStrokeWidth(2);
        } else {
            btnSecBlindado.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            btnSecBlindado.setTextColor(activeTextColor);
            btnSecBlindado.setStrokeWidth(0);

            btnSecNormal.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            btnSecNormal.setTextColor(inactiveTextColor);
            btnSecNormal.setStrokeColor(ColorStateList.valueOf(borderColor));
            btnSecNormal.setStrokeWidth(2);
        }
    }

    private void saveMe() {
        // Ocultar teclado al intentar procesar los datos
        hideKeyboard();

        String phoneNumber = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            showError("Ingresa un número válido con código de país");
            return;
        }

        if (phoneNumber.length() < 8) {
            showError("El número ingresado es muy corto");
            return;
        }

        tvError.setVisibility(View.GONE);

        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("user_phone", phoneNumber);
        editor.putInt("security_level", selectedSecurityLevel);
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}