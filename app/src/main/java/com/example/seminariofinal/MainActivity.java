package com.example.seminariofinal;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // Componentes de la vista
    private EditText etSearch;
    private RecyclerView rvChatList;
    private FloatingActionButton fabNewChat;
    private BottomNavigationView bottomNavigation;

    // Simulación de variables globales de tu script
    private boolean isUserLoggedIn = false; // Corresponde a `ME`
    private boolean isClientConnected = false; // Corresponde a `client`

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Leer el estado de la sesión desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("is_logged_in", false);

        // 1. Control de Onboarding
        if (!isUserLoggedIn) {
            showOnboarding();
            return;
        }

        setContentView(R.layout.activity_main);

        // Enlazar las vistas
        initViews();

        // 2. Reconstruir conversaciones e iniciar red
        rebuildConvs();
        if (!isClientConnected) {
            startNet();
        } else {
            subscribeToTopics();
        }

        // 3. Renderizar interfaz y listeners
        renderList("");
        setupListeners();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvChatList = findViewById(R.id.rvChatList);
        fabNewChat = findViewById(R.id.fabNewChat);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupListeners() {
        // Evento para el buscador ("oninput="renderList()")
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Evento para "Nuevo Chat" (onclick="openNewChat()")
        fabNewChat.setOnClickListener(v -> openNewChat());

        // Eventos para la navegación inferior (tabs)
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                goHome();
                return true;
            } else if (itemId == R.id.nav_profile) {
                openProfile();
                return true;
            }
            return false;
        });
    }

    private void showOnboarding() {
        // Redirigir a la pantalla de Login u Onboarding
        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void rebuildConvs() {
        // Reconstruir lista local desde la base de datos (p. ej. Room / SQLite)
    }

    private void startNet() {
        // Iniciar cliente de conexión (WebSockets o MQTT)
    }

    private void subscribeToTopics() {
        // Recorrer lista de tópicos/chats y suscribir el cliente
    }

    private void renderList(String query) {
        // Filtrar datos y actualizar el Adapter del RecyclerView
    }

    private void openNewChat() {
        // Lanzar Activity para iniciar una conversación nueva
    }

    private void goHome() {
        // Volver al inicio o hacer scroll arriba
    }

    private void openProfile() {
        // Lanzar Activity de Perfil
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}