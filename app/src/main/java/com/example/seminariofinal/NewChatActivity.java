package com.example.seminariofinal;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    private MaterialToolbar toolbarNewChat;
    private EditText etNcSearch;
    private Button btnAddContact, btnImportContacts, btnNewGroup;
    private RecyclerView rvNcList;

    private List<Contact> contactList = new ArrayList<>();
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        initViews();
        setupListeners();
        loadContacts();
        renderNewChat("");
    }

    private void initViews() {
        toolbarNewChat = findViewById(R.id.toolbarNewChat);
        etNcSearch = findViewById(R.id.etNcSearch);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnImportContacts = findViewById(R.id.btnImportContacts);
        btnNewGroup = findViewById(R.id.btnNewGroup);
        rvNcList = findViewById(R.id.rvNcList);

        rvNcList.setLayoutManager(new LinearLayoutManager(this));

        // 'true' activa los botones de editar y eliminar en NewChatActivity
        adapter = new ContactAdapter(new ArrayList<>(), true, new ContactAdapter.OnContactActionListener() {
            @Override
            public void onEdit(Contact contact, int position) {
                openEditContactModal(contact);
            }

            @Override
            public void onDelete(Contact contact, int position) {
                confirmDeleteContact(contact);
            }
        });
        rvNcList.setAdapter(adapter);
    }

    private void setupListeners() {
        toolbarNewChat.setNavigationOnClickListener(v -> finish());

        etNcSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderNewChat(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddContact.setOnClickListener(v -> openAddContactModal());
        btnImportContacts.setOnClickListener(v -> importToNew());
        btnNewGroup.setOnClickListener(v -> openNewGroupModal());
    }

    private void loadContacts() {
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

    private void renderNewChat(String query) {
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

    private void saveContactsToPrefs() {
        SharedPreferences prefs = getSharedPreferences("starssenger_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String updatedJson = gson.toJson(contactList);
        prefs.edit().putString("contacts_list", updatedJson).apply();
    }

    private void importToNew() {
        Toast.makeText(this, "Accediendo a la agenda...", Toast.LENGTH_SHORT).show();
    }

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
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void openEditContactModal(Contact contact) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_contact);

        EditText etCName = dialog.findViewById(R.id.etCName);
        EditText etCNum = dialog.findViewById(R.id.etCNum);
        Button btnSave = dialog.findViewById(R.id.btnSaveContact);
        Button btnCancel = dialog.findViewById(R.id.btnCancelContact);
        TextView tvCErr = dialog.findViewById(R.id.tvCErr);

        etCName.setText(contact.getName());
        etCNum.setText(contact.getPhone());
        btnSave.setText("Actualizar");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = etCName.getText().toString().trim();
            String newNum = etCNum.getText().toString().trim();

            if (newName.isEmpty() || newNum.isEmpty()) {
                tvCErr.setText("Completa todos los campos");
                tvCErr.setVisibility(View.VISIBLE);
            } else {
                contact.setName(newName);
                contact.setPhone(newNum);
                saveContactsToPrefs();
                renderNewChat(etNcSearch.getText().toString());
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void confirmDeleteContact(Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Estás seguro de que quieres eliminar a " + contact.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    contactList.remove(contact);
                    saveContactsToPrefs();
                    renderNewChat(etNcSearch.getText().toString());
                    Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

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
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void saveContact(String name, String phone) {
        contactList.add(new Contact(name, phone));
        saveContactsToPrefs();
        Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
        renderNewChat(etNcSearch.getText().toString());
    }

    private void saveGroup(String groupName) {}
}