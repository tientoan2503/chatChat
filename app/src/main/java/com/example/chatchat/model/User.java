package com.example.chatchat.model;

public class User {

    private String username;
    private String password;
    private String avatar;
    private String id;
    private String email;
    private int status;
    private String search;

    public User() {}

    public User(String name, String password, String avatar, String id, String email, int status, String search) {
        this.username = name;
        this.password = password;
        this.avatar = avatar;
        this.id = id;
        this.email = email;
        this.status = status;
        this.search = search;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
