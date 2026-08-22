package com.example.seminariofinal;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private MaterialToolbar toolbarChat;
    private TextView tvChName, tvChSub;
    private ImageButton btnVoiceCall, btnVideoCall, btnMic, btnSend;
    private EditText etTxt;
    private RecyclerView rvMessages;

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private AudioRecorderHelper recorderHelper = new AudioRecorderHelper();
    private String audioFilePath;
    private boolean isRecording = false;
    private String contactPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        getIntentData();
        loadMessages();
        setupListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMessages();
    }

    private void downloadAudioFile(String sourceFilePath) {
        if (sourceFilePath == null) return;

        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            Toast.makeText(this, "El archivo de audio no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            File destFile = new File(downloadsDir, "Audio_" + System.currentTimeMillis() + ".wav");

            FileInputStream in = new FileInputStream(sourceFile);
            FileOutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.close();

            Toast.makeText(this, "Audio guardado en Descargas", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al descargar el audio", Toast.LENGTH_SHORT).show();
        }
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

        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        messageAdapter = new MessageAdapter(messageList, new MessageAdapter.OnMessageActionListener() {
            @Override
            public void onDeleteMessage(Message message, int position) {
                deleteMessage(message, position);
            }

            @Override
            public void onDownloadAudio(Message message) {
                downloadAudioFile(message.getAudioPath());
            }
        });
        rvMessages.setAdapter(messageAdapter);
    }

    private void getIntentData() {
        if (getIntent() != null) {
            String name = getIntent().getStringExtra("contact_name");
            contactPhone = getIntent().getStringExtra("contact_phone");
            if (name != null && !name.isEmpty()) tvChName.setText(name);
            if (contactPhone != null && !contactPhone.isEmpty()) tvChSub.setText(contactPhone);
        }
    }

    private void loadMessages() {
        if (contactPhone == null || contactPhone.isEmpty()) return;
        SharedPreferences prefs = getSharedPreferences("starssenger_prefs", MODE_PRIVATE);
        String json = prefs.getString("chat_messages_" + contactPhone, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Message>>() {}.getType();
            List<Message> saved = gson.fromJson(json, type);
            if (saved != null) {
                messageList.clear();
                messageList.addAll(saved);
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }
        }
    }

    private void saveMessages() {
        if (contactPhone == null || contactPhone.isEmpty()) return;
        SharedPreferences prefs = getSharedPreferences("starssenger_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(messageList);
        prefs.edit().putString("chat_messages_" + contactPhone, json).apply();
    }

    private void setupListeners() {
        toolbarChat.setNavigationOnClickListener(v -> finish());

        etTxt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                toggleBarBtns(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(v -> send());
        btnMic.setOnClickListener(v -> toggleRec());
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
        String text = etTxt.getText().toString().trim();
        if (!text.isEmpty()) {
            Message message = new Message(UUID.randomUUID().toString(), text, null, Message.TYPE_TEXT, true);
            messageAdapter.addMessage(message);
            saveMessages();
            etTxt.setText("");
            rvMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void toggleRec() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        if (!isRecording) {
            startRecording();
        } else {
            stopRecordingAndSend();
        }
    }

    private void startRecording() {
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".wav";
        recorderHelper.startRecording(audioFilePath);
        isRecording = true;
        Toast.makeText(this, "Grabando audio... Toca el micro para detener y enviar", Toast.LENGTH_SHORT).show();
    }

    private void stopRecordingAndSend() {
        if (isRecording) {
            recorderHelper.stopRecording(audioFilePath);
            isRecording = false;

            File audioFile = new File(audioFilePath);
            if (audioFile.exists() && audioFile.length() > 44) {
                Message message = new Message(UUID.randomUUID().toString(), null, audioFilePath, Message.TYPE_AUDIO, true);
                messageAdapter.addMessage(message);
                saveMessages();
                rvMessages.scrollToPosition(messageList.size() - 1);
            } else {
                Toast.makeText(this, "El audio grabado está vacío", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteMessage(Message message, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar mensaje")
                .setMessage("¿Estás seguro de eliminar este mensaje?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (message.getType() == Message.TYPE_AUDIO && message.getAudioPath() != null) {
                        File file = new File(message.getAudioPath());
                        if (file.exists()) file.delete();
                    }
                    messageAdapter.removeMessage(position);
                    saveMessages();
                    Toast.makeText(this, "Mensaje eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleRec();
        }
    }
}