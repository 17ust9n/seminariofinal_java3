package com.example.seminariofinal;

public class Contact {
    private String name;
    private String phone;
    private String publicKey;

    // Constructor completo
    public Contact(String name, String phone, String publicKey) {
        this.name = name;
        this.phone = phone;
        this.publicKey = publicKey;
    }

    // Constructor sobrecargado para retrocompatibilidad
    public Contact(String name, String phone) {
        this(name, phone, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}