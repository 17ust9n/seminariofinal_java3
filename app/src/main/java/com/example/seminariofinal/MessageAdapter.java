package com.example.seminariofinal;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageActionListener {
        void onDeleteMessage(Message message, int position);
        void onDownloadAudio(Message message);
    }

    // Identificadores de Layout según Tipo + Emisor
    private static final int VIEW_TYPE_TEXT_SENT = 1;
    private static final int VIEW_TYPE_TEXT_RECEIVED = 2;
    private static final int VIEW_TYPE_AUDIO_SENT = 3;
    private static final int VIEW_TYPE_AUDIO_RECEIVED = 4;

    private List<Message> messageList;
    private final OnMessageActionListener listener;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;

    public MessageAdapter(List<Message> messageList, OnMessageActionListener listener) {
        this.messageList = messageList != null ? messageList : new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.getType() == Message.TYPE_AUDIO) {
            return msg.isSentByMe() ? VIEW_TYPE_AUDIO_SENT : VIEW_TYPE_AUDIO_RECEIVED;
        } else {
            return msg.isSentByMe() ? VIEW_TYPE_TEXT_SENT : VIEW_TYPE_TEXT_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case VIEW_TYPE_TEXT_SENT:
                view = inflater.inflate(R.layout.item_message_text_sent, parent, false);
                return new TextViewHolder(view);

            case VIEW_TYPE_TEXT_RECEIVED:
                view = inflater.inflate(R.layout.item_message_text_received, parent, false);
                return new TextViewHolder(view);

            case VIEW_TYPE_AUDIO_SENT:
                view = inflater.inflate(R.layout.item_message_audio_sent, parent, false);
                return new AudioViewHolder(view);

            case VIEW_TYPE_AUDIO_RECEIVED:
                view = inflater.inflate(R.layout.item_message_audio_received, parent, false);
                return new AudioViewHolder(view);

            default:
                view = inflater.inflate(R.layout.item_message_text_sent, parent, false);
                return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof TextViewHolder) {
            TextViewHolder textHolder = (TextViewHolder) holder;
            textHolder.tvMessageText.setText(message.getText());

            if (textHolder.btnDeleteMessage != null) {
                textHolder.btnDeleteMessage.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteMessage(message, holder.getAdapterPosition());
                });
            }

        } else if (holder instanceof AudioViewHolder) {
            AudioViewHolder audioHolder = (AudioViewHolder) holder;
            boolean isPlaying = (currentlyPlayingPosition == position);
            audioHolder.btnPlayAudio.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

            audioHolder.btnPlayAudio.setOnClickListener(v ->
                    playAudio(holder.itemView, message.getAudioPath(), holder.getAdapterPosition())
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

    private void playAudio(View itemView, String audioPath, int position) {
        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(itemView.getContext(), "Archivo de audio no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            int lastPos = currentlyPlayingPosition;
            currentlyPlayingPosition = -1;
            if (lastPos != -1) notifyItemChanged(lastPos);

            if (lastPos == position) return; // Si era el mismo, solo lo pausas
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentlyPlayingPosition = position;
            notifyItemChanged(position);

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                int completedPos = currentlyPlayingPosition;
                currentlyPlayingPosition = -1;
                if (completedPos != -1) notifyItemChanged(completedPos);
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(itemView.getContext(), "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
        }
    }

    // Método crucial para llamar desde el onDestroy/onStop de ChatActivity y liberar memoria
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            currentlyPlayingPosition = -1;
        }
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