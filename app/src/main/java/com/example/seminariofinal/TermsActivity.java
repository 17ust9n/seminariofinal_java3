package com.example.seminariofinal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        MaterialToolbar toolbar = findViewById(R.id.toolbarTerms);

        // Maneja la acción del botón de regresar (flecha atrás)
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}