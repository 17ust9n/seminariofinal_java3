package com.example.seminariofinal;

public class Message {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_AUDIO = 2;

    private String id;
    private String text;
    private String audioPath;
    private int type; // TYPE_TEXT o TYPE_AUDIO
    private boolean isSentByMe;
    private long timestamp;

    // Campos para integración con Libsodium E2EE
    private boolean isEncrypted;
    private byte[] nonce; // Nonce (24 bytes) usado para cifrar/descifrar con Libsodium

    public Message(String id, String text, String audioPath, int type, boolean isSentByMe) {
        this.id = id;
        this.text = text;
        this.audioPath = audioPath;
        this.type = type;
        this.isSentByMe = isSentByMe;
        this.timestamp = System.currentTimeMillis();
        this.isEncrypted = false;
        this.nonce = null;
    }

    // Constructor sobrecargado para mensajes cifrados con Libsodium
    public Message(String id, String text, String audioPath, int type, boolean isSentByMe, boolean isEncrypted, byte[] nonce) {
        this(id, text, audioPath, type, isSentByMe);
        this.isEncrypted = isEncrypted;
        this.nonce = nonce;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getText() { return text; }
    public String getAudioPath() { return audioPath; }
    public int getType() { return type; }
    public boolean isSentByMe() { return isSentByMe; }
    public long getTimestamp() { return timestamp; }

    public boolean isEncrypted() { return isEncrypted; }
    public void setEncrypted(boolean encrypted) { isEncrypted = encrypted; }

    public byte[] getNonce() { return nonce; }
    public void setNonce(byte[] nonce) { this.nonce = nonce; }

    // Setter para actualizar el texto en claro tras el descifrado en memoria
    public void setText(String text) {
        this.text = text;
    }

    // Setter para actualizar la ruta de audio tras descifrar el archivo temporal
    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}