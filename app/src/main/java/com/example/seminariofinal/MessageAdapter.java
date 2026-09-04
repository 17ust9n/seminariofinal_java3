package com.example.seminariofinal;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goterl.lazysodium.utils.Key;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageActionListener {
        void onDeleteMessage(Message message, int position);
        void onDownloadAudio(Message message);
    }

    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_AUDIO = 2;

    private List<Message> messageList;
    private final OnMessageActionListener listener;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;

    // Helper y Clave para descifrado de audios cifrados
    private AudioRecorderHelper audioHelper;
    private Key sessionKey;
    private String currentTempDecryptedPath = null;

    public MessageAdapter(List<Message> messageList, OnMessageActionListener listener) {
        this.messageList = messageList != null ? messageList : new ArrayList<>();
        this.listener = listener;
        this.audioHelper = new AudioRecorderHelper();
    }

    public void setSessionKey(Key sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        return (msg.getType() == Message.TYPE_AUDIO) ? VIEW_TYPE_AUDIO : VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_AUDIO) {
            View view = inflater.inflate(R.layout.item_message_audio, parent, false);
            return new AudioViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_text, parent, false);
            return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof TextViewHolder) {
            TextViewHolder textHolder = (TextViewHolder) holder;
            textHolder.tvMessageText.setText(message.getText());

            // Alinear a la derecha si es enviado, a la izquierda si es recibido
            LinearLayout rootLayout = (LinearLayout) holder.itemView;
            rootLayout.setGravity(message.isSentByMe() ? Gravity.END : Gravity.START);

            if (textHolder.btnDeleteMessage != null) {
                textHolder.btnDeleteMessage.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteMessage(message, holder.getAdapterPosition());
                });
            }

        } else if (holder instanceof AudioViewHolder) {
            AudioViewHolder audioHolder = (AudioViewHolder) holder;

            // Alinear a la derecha si es enviado, a la izquierda si es recibido
            LinearLayout rootLayout = (LinearLayout) holder.itemView;
            rootLayout.setGravity(message.isSentByMe() ? Gravity.END : Gravity.START);

            boolean isPlaying = (currentlyPlayingPosition == position);
            audioHolder.btnPlayAudio.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

            audioHolder.btnPlayAudio.setOnClickListener(v ->
                    playAudio(holder.itemView, message, holder.getAdapterPosition())
            );

            if (audioHolder.btnDownloadAudio != null) {
                audioHolder.btnDownloadAudio.setOnClickListener(v -> {
                    if (listener != null) listener.onDownloadAudio(message);
                });
            }

            if (audioHolder.btnDeleteMessage != null) {
                audioHolder.btnDeleteMessage.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteMessage(message, holder.getAdapterPosition());
                });
            }
        }
    }

    private void playAudio(View itemView, Message message, int position) {
        String audioPath = message.getAudioPath();

        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(itemView.getContext(), "Archivo de audio no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        stopAndCleanPlayer();

        if (currentlyPlayingPosition == position) {
            currentlyPlayingPosition = -1;
            notifyItemChanged(position);
            return;
        }

        String pathToPlay = audioPath;

        if (message.isEncrypted() || audioPath.endsWith(".enc")) {
            if (sessionKey == null || message.getNonce() == null) {
                Toast.makeText(itemView.getContext(), "No se puede descifrar el audio (Falta clave/nonce)", Toast.LENGTH_SHORT).show();
                return;
            }

            File tempFile = new File(itemView.getContext().getCacheDir(), "temp_play_" + System.currentTimeMillis() + ".wav");
            currentTempDecryptedPath = tempFile.getAbsolutePath();

            boolean success = audioHelper.decryptFile(audioPath, currentTempDecryptedPath, sessionKey, message.getNonce());
            if (!success) {
                Toast.makeText(itemView.getContext(), "Error al descifrar el archivo de audio", Toast.LENGTH_SHORT).show();
                cleanTempFile();
                return;
            }

            pathToPlay = currentTempDecryptedPath;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(pathToPlay);
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentlyPlayingPosition = position;
            notifyItemChanged(position);

            mediaPlayer.setOnCompletionListener(mp -> {
                stopAndCleanPlayer();
                int completedPos = currentlyPlayingPosition;
                currentlyPlayingPosition = -1;
                if (completedPos != -1) notifyItemChanged(completedPos);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(itemView.getContext(), "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
            stopAndCleanPlayer();
        }
    }

    private void stopAndCleanPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        cleanTempFile();
    }

    private void cleanTempFile() {
        if (currentTempDecryptedPath != null) {
            File file = new File(currentTempDecryptedPath);
            if (file.exists()) {
                file.delete();
            }
            currentTempDecryptedPath = null;
        }
    }

    public void releaseMediaPlayer() {
        stopAndCleanPlayer();
        currentlyPlayingPosition = -1;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void removeMessage(int position) {
        if (position >= 0 && position < messageList.size()) {
            messageList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, messageList.size());
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText;
        ImageButton btnDeleteMessage;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            btnDeleteMessage = itemView.findViewById(R.id.btnDeleteMessage);
        }
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnPlayAudio, btnDownloadAudio, btnDeleteMessage;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            btnPlayAudio = itemView.findViewById(R.id.btnPlayAudio);
            btnDownloadAudio = itemView.findViewById(R.id.btnDownloadAudio);
            btnDeleteMessage = itemView.findViewById(R.id.btnDeleteMessage);
        }
    }
}