package com.example.seminariofinal;

public class Message {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_AUDIO = 2;

    private String id;
    private String text;
    private String audioPath;
    private int type; // TYPE_TEXT o TYPE_AUDIO
    private boolean isSentByMe;

    public Message(String id, String text, String audioPath, int type, boolean isSentByMe) {
        this.id = id;
        this.text = text;
        this.audioPath = audioPath;
        this.type = type;
        this.isSentByMe = isSentByMe;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getAudioPath() { return audioPath; }
    public int getType() { return type; }
    public boolean isSentByMe() { return isSentByMe; }
}