package com.example.seminariofinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

public class ChatActivity extends AppCompatActivity {

    private MaterialToolbar toolbarChat;
    private TextView tvChName, tvChSub;
    private ImageButton btnVoiceCall, btnVideoCall, btnMic, btnSend;
    private EditText etTxt;
    private RecyclerView rvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupListeners();
    }

    private void initViews() {
        toolbarChat = findViewById(R.id.toolbarChat);
        tvChName = findViewById(R.id.tvChName);
        tvChSub = findViewById(R.id.tvChSub);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);
        btnMic = findViewById(R.id.btnMic);
        btnSend = findViewById(R.id.btnSend);
        etTxt = findViewById(R.id.etTxt);
        rvMessages = findViewById(R.id.rvMessages);
    }

    private void setupListeners() {
        toolbarChat.setNavigationOnClickListener(v -> finish());

        // Lógica para alternar botón de enviar y micrófono (toggleBarBtns)
        etTxt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                toggleBarBtns(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Enviar con tecla "Enter" o botón "Send"
        etTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(v -> send());
        btnMic.setOnClickListener(v -> toggleRec());

        // Llamadas
        btnVoiceCall.setOnClickListener(v -> voiceCurrent());
        btnVideoCall.setOnClickListener(v -> callCurrent());
    }

    private void toggleBarBtns(String text) {
        if (!text.isEmpty()) {
            btnSend.setVisibility(View.VISIBLE);
            btnMic.setVisibility(View.GONE);
        } else {
            btnSend.setVisibility(View.GONE);
            btnMic.setVisibility(View.VISIBLE);
        }
    }

    private void send() {
        String message = etTxt.getText().toString().trim();
        if (!message.isEmpty()) {
            // Cifrar y transmitir el mensaje
            etTxt.setText("");
        }
    }

    private void toggleRec() {
        // Lógica para iniciar/detener grabación de audio
    }

    private void voiceCurrent() {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("IS_VIDEO", false);
        startActivity(intent);
    }

    private void callCurrent() {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("IS_VIDEO", true);
        startActivity(intent);
    }
}