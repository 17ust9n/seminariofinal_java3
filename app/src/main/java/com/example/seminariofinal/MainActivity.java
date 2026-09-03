package com.example.seminariofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importaciones de Lazysodium
import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.SodiumAndroid;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvChatList;
    private FloatingActionButton fabNewChat;
    private BottomNavigationView bottomNavigation;

    private boolean isUserLoggedIn = false;
    private boolean isClientConnected = false;
    private List<Contact> contactList = new ArrayList<>();
    private ContactAdapter adapter;

    // Instancia global de Lazysodium para usar cifrado en esta Activity
    private LazySodiumAndroid lazySodium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("starssenger_prefs", Context.MODE_PRIVATE);
        isUserLoggedIn = preferences.getBoolean("is_logged_in", false);

        if (!isUserLoggedIn) {
            showOnboarding();
            return;
        }

        setContentView(R.layout.activity_main);

        // Inicializar Lazysodium con el motor nativo de Android
        lazySodium = new LazySodiumAndroid(new SodiumAndroid());

        initViews();

        if (!isClientConnected) {
            startNet();
        } else {
            subscribeToTopics();
        }

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rebuildConvs();
        renderList(etSearch.getText().toString());

        // Asegurar que la pestaña activa sea Chats (Home) al volver
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvChatList = findViewById(R.id.rvChatList);
        fabNewChat = findViewById(R.id.fabNewChat);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvChatList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactAdapter(new ArrayList<>(), false, new ContactAdapter.OnContactActionListener() {
            @Override
            public void onContactClick(Contact contact) {
                hideKeyboard();
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("contact_name", contact.getName());
                intent.putExtra("contact_phone", contact.getPhone());
                startActivity(intent);
            }

            @Override
            public void onEdit(Contact contact, int position) {}

            @Override
            public void onDelete(Contact contact, int position) {}
        });

        rvChatList.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabNewChat.setOnClickListener(v -> openNewChat());

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Ya estás en la pantalla principal
                return true;
            } else if (itemId == R.id.nav_profile) {
                hideKeyboard();
                openProfile();
                return true;
            }
            return false;
        });
    }

    private void showOnboarding() {
        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void rebuildConvs() {
        SharedPreferences prefs = getSharedPreferences("starssenger_prefs", MODE_PRIVATE);
        String json = prefs.getString("contacts_list", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
            contactList = gson.fromJson(json, type);
        } else {
            contactList = new ArrayList<>();
        }
    }

    private void renderList(String query) {
        List<Contact> filteredList = new ArrayList<>();
        for (Contact c : contactList) {
            if (c.getName().toLowerCase().contains(query.toLowerCase()) ||
                    c.getPhone().contains(query)) {
                filteredList.add(c);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
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

    private void startNet() {}
    private void subscribeToTopics() {}

    private void openNewChat() {
        hideKeyboard();
        Intent intent = new Intent(MainActivity.this, NewChatActivity.class);
        startActivity(intent);
    }

    private void goHome() {}

    private void openProfile() {
        hideKeyboard();
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    // Getter para obtener la instancia desde otras partes si fuere necesario
    public LazySodiumAndroid getLazySodium() {
        return lazySodium;
    }
}