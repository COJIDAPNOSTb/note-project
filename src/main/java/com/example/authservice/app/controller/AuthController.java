package com.example.authservice.app.controller;


import com.example.authservice.app.model.dto.JwtResponseDto;
import com.example.authservice.app.model.dto.UserLoginDto;
import com.example.authservice.app.model.dto.UserRegisterDto;
import com.example.authservice.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(@RequestBody UserRegisterDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody UserLoginDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
