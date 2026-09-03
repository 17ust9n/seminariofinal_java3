package com.example.seminariofinal;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

        adapter = new ContactAdapter(new ArrayList<>(), true, new ContactAdapter.OnContactActionListener() {
            @Override
            public void onContactClick(Contact contact) {
                hideKeyboard();
                Intent intent = new Intent(NewChatActivity.this, ChatActivity.class);
                intent.putExtra("contact_name", contact.getName());
                intent.putExtra("contact_phone", contact.getPhone());
                // Pasar la clave pública del contacto a ChatActivity
                intent.putExtra("contact_public_key", contact.getPublicKey());
                startActivity(intent);
            }

            @Override
            public void onEdit(Contact contact, int position) {
                hideKeyboard();
                openEditContactModal(contact);
            }

            @Override
            public void onDelete(Contact contact, int position) {
                hideKeyboard();
                confirmDeleteContact(contact);
            }
        });
        rvNcList.setAdapter(adapter);
    }

    private void setupListeners() {
        toolbarNewChat.setNavigationOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        etNcSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderNewChat(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddContact.setOnClickListener(v -> {
            hideKeyboard();
            openAddContactModal();
        });

        btnImportContacts.setOnClickListener(v -> {
            hideKeyboard();
            importToNew();
        });

        btnNewGroup.setOnClickListener(v -> {
            hideKeyboard();
            openNewGroupModal();
        });
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
        Dialog dialog = createStyledDialog(R.layout.dialog_add_contact);

        EditText etCName = dialog.findViewById(R.id.etCName);
        EditText etCNum = dialog.findViewById(R.id.etCNum);
        EditText etCPublicKey = dialog.findViewById(R.id.etCPublicKey);
        Button btnSave = dialog.findViewById(R.id.btnSaveContact);
        Button btnCancel = dialog.findViewById(R.id.btnCancelContact);
        TextView tvCErr = dialog.findViewById(R.id.tvCErr);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etCName.getText().toString().trim();
            String num = etCNum.getText().toString().trim();
            String pubKey = etCPublicKey != null ? etCPublicKey.getText().toString().trim() : "";

            if (name.isEmpty() || num.isEmpty()) {
                tvCErr.setText("Completa nombre y número de teléfono");
                tvCErr.setVisibility(View.VISIBLE);
            } else {
                saveContact(name, num, pubKey);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openEditContactModal(Contact contact) {
        Dialog dialog = createStyledDialog(R.layout.dialog_add_contact);

        EditText etCName = dialog.findViewById(R.id.etCName);
        EditText etCNum = dialog.findViewById(R.id.etCNum);
        EditText etCPublicKey = dialog.findViewById(R.id.etCPublicKey);
        Button btnSave = dialog.findViewById(R.id.btnSaveContact);
        Button btnCancel = dialog.findViewById(R.id.btnCancelContact);
        TextView tvCErr = dialog.findViewById(R.id.tvCErr);

        etCName.setText(contact.getName());
        etCNum.setText(contact.getPhone());
        if (etCPublicKey != null && contact.getPublicKey() != null) {
            etCPublicKey.setText(contact.getPublicKey());
        }
        btnSave.setText("Actualizar");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = etCName.getText().toString().trim();
            String newNum = etCNum.getText().toString().trim();
            String newPubKey = etCPublicKey != null ? etCPublicKey.getText().toString().trim() : "";

            if (newName.isEmpty() || newNum.isEmpty()) {
                tvCErr.setText("Completa nombre y número de teléfono");
                tvCErr.setVisibility(View.VISIBLE);
            } else {
                contact.setName(newName);
                contact.setPhone(newNum);
                contact.setPublicKey(newPubKey);
                saveContactsToPrefs();
                renderNewChat(etNcSearch.getText().toString());
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
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
        Dialog dialog = createStyledDialog(R.layout.dialog_new_group);

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

    private Dialog createStyledDialog(int layoutResId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(layoutResId);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    private void saveContact(String name, String phone, String publicKey) {
        contactList.add(new Contact(name, phone, publicKey));
        saveContactsToPrefs();
        Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
        renderNewChat(etNcSearch.getText().toString());
    }

    private void saveGroup(String groupName) {}

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}