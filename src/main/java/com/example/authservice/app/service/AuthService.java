package com.example.authservice.app.service;



import com.example.authservice.app.model.Role;
import com.example.authservice.app.model.User;
import com.example.authservice.app.model.dto.JwtResponseDto;
import com.example.authservice.app.model.dto.UserLoginDto;
import com.example.authservice.app.model.dto.UserRegisterDto;
import com.example.authservice.app.repository.UserRepository;
import com.example.authservice.app.security.CustomUserDetails;
import com.example.authservice.app.security.JwtService;
import com.example.authservice.app.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtResponseDto register(UserRegisterDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER)) // DEFAULT
                .build();

        userRepository.save(user);
        String jwt = jwtService.generateToken(new CustomUserDetails(user));
        return new JwtResponseDto(jwt);
    }


    public JwtResponseDto login(UserLoginDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        String jwt = jwtService.generateToken(new CustomUserDetails(user));
        return new JwtResponseDto(jwt);
    }
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token);
        }
    }
}
