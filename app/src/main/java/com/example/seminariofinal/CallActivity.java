package com.example.seminariofinal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CallActivity extends AppCompatActivity {

    private LinearLayout layoutInCall;
    private TextView tvIcName, tvVfName, tvVfState;
    private Button btnDeclineCall, btnAnswerCall;
    private FloatingActionButton fabAddCall, fabHangup;

    private boolean isPendingCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        initViews();
        setupListeners();
        checkPendingCall();
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
    }

    private void setupListeners() {
        fabHangup.setOnClickListener(v -> hangup());
        fabAddCall.setOnClickListener(v -> openInviteModal());

        btnDeclineCall.setOnClickListener(v -> declineCall());
        btnAnswerCall.setOnClickListener(v -> answerCall());
    }

    private void checkPendingCall() {
        // Simula la verificación de pendingCall en JS
        if (isPendingCall) {
            tvIcName.setText("Llamada de Contacto");
            layoutInCall.setVisibility(View.VISIBLE);
        }
    }

    private void hangup() {
        finish(); // Finaliza la llamada y cierra la Activity
    }

    private void openInviteModal() {
        // Modal: Invitar a la llamada (mInvite)
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_invite_call);

        Button btnInvite = dialog.findViewById(R.id.btnDoInvite);
        Button btnCancel = dialog.findViewById(R.id.btnCancelInvite);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnInvite.setOnClickListener(v -> {
            // Lógica para sumar contacto
            dialog.dismiss();
        });

        dialog.show();
    }

    private void declineCall() {
        layoutInCall.setVisibility(View.GONE);
        finish();
    }

    private void answerCall() {
        layoutInCall.setVisibility(View.GONE);
        tvVfState.setText("En llamada");
    }
}