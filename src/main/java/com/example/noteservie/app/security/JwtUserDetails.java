package com.example.noteservie.app.security;
public class JwtUserDetails {

    private final Long id;
    private final String username;

    public JwtUserDetails(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
