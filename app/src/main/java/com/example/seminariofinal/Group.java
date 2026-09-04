package com.example.seminariofinal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private String id;
    private String name;
    private List<Contact> members;

    public Group(String name, List<Contact> members) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.members = members != null ? members : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Contact> getMembers() { return members; }

    public void setName(String name) { this.name = name; }
    public void setMembers(List<Contact> members) { this.members = members; }
}