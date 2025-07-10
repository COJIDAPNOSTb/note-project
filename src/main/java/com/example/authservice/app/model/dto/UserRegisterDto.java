package com.example.authservice.app.model.dto;


import lombok.Data;

import java.util.Set;

@Data
public class UserRegisterDto {
    private String username;
    private String password;
    private Set<String> roles;
}
