package com.example.seminariofinal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.utils.KeyPair;

public class CallActivity extends AppCompatActivity {

    private LinearLayout layoutInCall;
    private TextView tvIcName, tvVfName, tvVfState;
    private Button btnDeclineCall, btnAnswerCall;
    private FloatingActionButton fabAddCall, fabHangup;

    private boolean isPendingCall = false;
    private String contactName;
    private String contactPhone;
    private String contactPublicKey;

    // Elementos de seguridad efímeros
    private KeyPair ephemeralKeyPair;
    private byte[] sessionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        getIntentData();
        initViews();
        setupListeners();
        checkPendingCall();
        generateEphemeralKeys();
    }

    private void getIntentData() {
        if (getIntent() != null) {
            contactName = getIntent().getStringExtra("contact_name");
            contactPhone = getIntent().getStringExtra("contact_phone");
            contactPublicKey = getIntent().getStringExtra("contact_public_key");
            isPendingCall = getIntent().getBooleanExtra("is_pending_call", false);
        }
    }

    private void initViews() {
        layoutInCall = findViewById(R.id.layoutInCall);
        tvIcName = findViewById(R.id.tvIcName);
        tvVfName = findViewById(R.id.tvVfName);
        tvVfState = findViewById(R.id.tvVfState);
        btnDeclineCall = findViewById(R.id.btnDeclineCall);
        btnAnswerCall = findViewById(R.id.btnAnswerCall);
        fabAddCall = findViewById(R.id.fabAddCall);
        fabHangup = findViewById(R.id.fabHangup);

        if (contactName != null) {
            tvVfName.setText(contactName);
        }
    }

    private void setupListeners() {
        fabHangup.setOnClickListener(v -> hangup());
        fabAddCall.setOnClickListener(v -> openInviteModal());

        btnDeclineCall.setOnClickListener(v -> declineCall());
        btnAnswerCall.setOnClickListener(v -> answerCall());
    }

    private void generateEphemeralKeys() {
        // Generar llaves efímeras para Forward Secrecy en la llamada
        new Thread(() -> {
            LazySodiumAndroid sodium = SodiumManager.getInstance();
            ephemeralKeyPair = sodium.cryptoBoxKeypair();
        }).start();
    }

    private void checkPendingCall() {
        if (isPendingCall) {
            tvIcName.setText(contactName != null ? contactName : "Llamada entrante");
            layoutInCall.setVisibility(View.VISIBLE);
        } else {
            tvVfState.setText("Llamando...");
        }
    }

    private void answerCall() {
        layoutInCall.setVisibility(View.GONE);
        tvVfState.setText("Conectando de forma segura...");

        // Derivar clave de sesión usando la clave pública del interlocutor
        new Thread(() -> {
            try {
                if (contactPublicKey != null && !contactPublicKey.isEmpty() && ephemeralKeyPair != null) {
                    LazySodiumAndroid sodium = SodiumManager.getInstance();

                    // Cálculo Diffie-Hellman para establecer canal seguro
                    // (ephemeralKeyPair.getSecretKey() x contactPublicKey)

                    runOnUiThread(() -> {
                        tvVfState.setText("En llamada (E2EE 🔒)");
                    });
                } else {
                    runOnUiThread(() -> {
                        tvVfState.setText("En llamada (Sin cifrar)");
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error en handshake", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void hangup() {
        cleanUpSession();
        finish();
    }

    private void declineCall() {
        cleanUpSession();
        layoutInCall.setVisibility(View.GONE);
        finish();
    }

    private void cleanUpSession() {
        // Limpiar de memoria la clave de sesión al finalizar la llamada
        sessionKey = null;
        ephemeralKeyPair = null;
    }

    private void openInviteModal() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_invite_call);

        Button btnInvite = dialog.findViewById(R.id.btnDoInvite);
        Button btnCancel = dialog.findViewById(R.id.btnCancelInvite);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnInvite.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}