package com.example.authservice.app.model.dto;


import lombok.Data;

@Data
public class UserLoginDto {
    private String username;
    private String password;
}
