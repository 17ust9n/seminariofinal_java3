package com.example.seminariofinal;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

public class NewChatActivity extends AppCompatActivity {

    private MaterialToolbar toolbarNewChat;
    private EditText etNcSearch;
    private Button btnAddContact, btnImportContacts, btnNewGroup;
    private RecyclerView rvNcList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        initViews();
        setupListeners();
        renderNewChat("");
    }

    private void initViews() {
        toolbarNewChat = findViewById(R.id.toolbarNewChat);
        etNcSearch = findViewById(R.id.etNcSearch);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnImportContacts = findViewById(R.id.btnImportContacts);
        btnNewGroup = findViewById(R.id.btnNewGroup);
        rvNcList = findViewById(R.id.rvNcList);
    }

    private void setupListeners() {
        toolbarNewChat.setNavigationOnClickListener(v -> finish());

        // Filtro en tiempo real (oninput="renderNewChat()")
        etNcSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderNewChat(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Modales
        btnAddContact.setOnClickListener(v -> openAddContactModal());
        btnImportContacts.setOnClickListener(v -> importToNew());
        btnNewGroup.setOnClickListener(v -> openNewGroupModal());
    }

    private void renderNewChat(String query) {
        // Filtrar y actualizar el Adapter del RecyclerView de contactos
    }

    private void importToNew() {
        // Lógica de lectura de contactos nativos de Android mediante ContactsContract
        Toast.makeText(this, "Accediendo a la agenda...", Toast.LENGTH_SHORT).show();
    }

    // MODAL: Agregar Nuevo Contacto (mContact)
    private void openAddContactModal() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_contact);

        EditText etCName = dialog.findViewById(R.id.etCName);
        EditText etCNum = dialog.findViewById(R.id.etCNum);
        Button btnSave = dialog.findViewById(R.id.btnSaveContact);
        Button btnCancel = dialog.findViewById(R.id.btnCancelContact);
        TextView tvCErr = dialog.findViewById(R.id.tvCErr);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etCName.getText().toString().trim();
            String num = etCNum.getText().toString().trim();

            if (name.isEmpty() || num.isEmpty()) {
                tvCErr.setText("Completa todos los campos");
                tvCErr.setVisibility(View.VISIBLE);
            } else {
                saveContact(name, num);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // MODAL: Nuevo Grupo (mGroup)
    private void openNewGroupModal() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_group);

        EditText etGName = dialog.findViewById(R.id.etGName);
        Button btnCreateGroup = dialog.findViewById(R.id.btnCreateGroup);
        Button btnCancelGroup = dialog.findViewById(R.id.btnCancelGroup);

        btnCancelGroup.setOnClickListener(v -> dialog.dismiss());
        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                saveGroup(groupName);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveContact(String name, String phone) {
        // Guardar contacto en SQLite/Room o SharedPreferences
    }

    private void saveGroup(String groupName) {
        // Crear el grupo en la base de datos local y emitir evento
    }
}