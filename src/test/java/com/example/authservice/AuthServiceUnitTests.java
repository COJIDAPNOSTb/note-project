package com.example.authservice;


import com.example.authservice.app.model.User;
import com.example.authservice.app.model.dto.JwtResponseDto;
import com.example.authservice.app.model.dto.UserLoginDto;
import com.example.authservice.app.model.dto.UserRegisterDto;
import com.example.authservice.app.repository.UserRepository;
import com.example.authservice.app.security.JwtService;
import com.example.authservice.app.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.authservice.app.model.Role;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private UserRegisterDto registerDto;
    private UserLoginDto loginDto;
    private User user;

    @BeforeEach
    void setUp() {
        registerDto = new UserRegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("password123");

        loginDto = new UserLoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("password123");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .build();
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Act
        JwtResponseDto result = authService.register(registerDto);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        verify(userRepository).existsByUsername(registerDto.getUsername());
        verify(passwordEncoder).encode(registerDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(registerDto));

        verify(userRepository).existsByUsername(registerDto.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void shouldLoginUserSuccessfully() {

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");


        JwtResponseDto result = authService.login(loginDto);


        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void shouldThrowExceptionWhenLoginCredentialsAreInvalid() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));


        assertThrows(BadCredentialsException.class, () -> authService.login(loginDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void shouldValidateRegistrationInput() {
        // Test null username
        UserRegisterDto nullUsername = new UserRegisterDto();
        nullUsername.setUsername(null);
        nullUsername.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> authService.register(nullUsername));

        // Test empty username
        UserRegisterDto emptyUsername = new UserRegisterDto();
        emptyUsername.setUsername("");
        emptyUsername.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> authService.register(emptyUsername));

        // Test null password
        UserRegisterDto nullPassword = new UserRegisterDto();
        nullPassword.setUsername("username");
        nullPassword.setPassword(null);

        assertThrows(IllegalArgumentException.class, () -> authService.register(nullPassword));

        // Test empty password
        UserRegisterDto emptyPassword = new UserRegisterDto();
        emptyPassword.setUsername("username");
        emptyPassword.setPassword("");

        assertThrows(IllegalArgumentException.class, () -> authService.register(emptyPassword));
    }

    @Test
    void shouldValidateLoginInput() {
        // Test null username
        UserLoginDto nullUsername = new UserLoginDto();
        nullUsername.setUsername(null);
        nullUsername.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> authService.login(nullUsername));

        // Test empty username
        UserLoginDto emptyUsername = new UserLoginDto();
        emptyUsername.setUsername("");
        emptyUsername.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> authService.login(emptyUsername));

        // Test null password
        UserLoginDto nullPassword = new UserLoginDto();
        nullPassword.setUsername("username");
        nullPassword.setPassword(null);

        assertThrows(IllegalArgumentException.class, () -> authService.login(nullPassword));

        // Test empty password
        UserLoginDto emptyPassword = new UserLoginDto();
        emptyPassword.setUsername("username");
        emptyPassword.setPassword("");

        assertThrows(IllegalArgumentException.class, () -> authService.login(emptyPassword));
    }

    @Test
    void shouldCreateUserWithCorrectRoles() {
        // Arrange
        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Capture the user being saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Act
        authService.register(registerDto);

        // Assert
        verify(userRepository).save(argThat(user -> {
            return user.getUsername().equals("testuser") &&
                    user.getPassword().equals("encodedPassword") &&
                    user.getRoles().contains(Role.USER);
        }));
    }

    @Test
    void shouldHandleSpecialCharactersInUsername() {

        UserRegisterDto emailDto = new UserRegisterDto();
        emailDto.setUsername("user@domain.com");
        emailDto.setPassword("password123");

        when(userRepository.existsByUsername(emailDto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(emailDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        JwtResponseDto result = authService.register(emailDto);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        verify(userRepository).existsByUsername("user@domain.com");
    }

}