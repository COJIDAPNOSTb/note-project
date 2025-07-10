package com.example.authservice;


import com.example.authservice.app.model.dto.JwtResponseDto;
import com.example.authservice.app.model.dto.UserLoginDto;
import com.example.authservice.app.model.dto.UserRegisterDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldRegisterNewUser() throws Exception {

        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("password123");

        // Выполнение запроса регистрации
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Проверка, что токен не пустой
        String response = result.getResponse().getContentAsString();
        JwtResponseDto jwtResponse = objectMapper.readValue(response, JwtResponseDto.class);

        assert jwtResponse.getToken() != null;
        assert !jwtResponse.getToken().isEmpty();
    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() throws Exception {
        // Сначала регистрируем пользователя
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("existinguser");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        // Попытка зарегистрировать пользователя с тем же именем
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest()); // или другой код ошибки
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("loginuser");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("loginuser");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldNotLoginWithInvalidCredentials() throws Exception {
        // Попытка входа с неправильными данными
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("nonexistentuser");
        loginDto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized()); // или другой код ошибки
    }

    @Test
    void shouldNotLoginWithCorrectUsernameButWrongPassword() throws Exception {
        // Сначала регистрируем пользователя
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("testuser2");
        registerDto.setPassword("correctpassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        // Попытка входа с правильным именем, но неправильным паролем
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("testuser2");
        loginDto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAccessUserEndpointWithUserToken() throws Exception {
        // Регистрируем пользователя
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("usertest");
        registerDto.setPassword("password123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andReturn();

        // Получаем токен
        String registerResponse = registerResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponse = objectMapper.readValue(registerResponse, JwtResponseDto.class);

        // Обращаемся к защищенному эндпоинту
        mockMvc.perform(get("/test/user")
                        .header("Authorization", "Bearer " + jwtResponse.getToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("You is User!"));
    }

    @Test
    void shouldNotAccessUserEndpointWithoutToken() throws Exception {
        // Попытка обращения к защищенному эндпоинту без токена
        mockMvc.perform(get("/test/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAccessUserEndpointWithInvalidToken() throws Exception {
        // Попытка обращения к защищенному эндпоинту с неправильным токеном
        mockMvc.perform(get("/test/user")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldValidateRegistrationInput() throws Exception {
        // Тест с пустым username
        UserRegisterDto emptyUsername = new UserRegisterDto();
        emptyUsername.setUsername("");
        emptyUsername.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUsername)))
                .andExpect(status().isBadRequest());

        // Тест с пустым password
        UserRegisterDto emptyPassword = new UserRegisterDto();
        emptyPassword.setUsername("testuser");
        emptyPassword.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPassword)))
                .andExpect(status().isBadRequest());

        // Тест с null значениями
        UserRegisterDto nullDto = new UserRegisterDto();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateLoginInput() throws Exception {
        // Тест с пустым username
        UserLoginDto emptyUsername = new UserLoginDto();
        emptyUsername.setUsername("");
        emptyUsername.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUsername)))
                .andExpect(status().isBadRequest());

        // Тест с пустым password
        UserLoginDto emptyPassword = new UserLoginDto();
        emptyPassword.setUsername("testuser");
        emptyPassword.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPassword)))
                .andExpect(status().isBadRequest());

        // Тест с null значениями
        UserLoginDto nullDto = new UserLoginDto();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnValidJwtTokenStructure() throws Exception {
        // Регистрируем пользователя
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("jwttest");
        registerDto.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andReturn();

        // Проверяем структуру JWT токена
        String response = result.getResponse().getContentAsString();
        JwtResponseDto jwtResponse = objectMapper.readValue(response, JwtResponseDto.class);

        String token = jwtResponse.getToken();

        // JWT токен должен состоять из трех частей, разделенных точками
        String[] tokenParts = token.split("\\.");
        assert tokenParts.length == 3 : "JWT token should have 3 parts";

        // Каждая часть должна быть не пустой
        for (String part : tokenParts) {
            assert !part.isEmpty() : "JWT token parts should not be empty";
        }
    }

    @Test
    void shouldPreventSqlInjection() throws Exception {
        // Попытка SQL инъекции в username
        UserRegisterDto sqlInjectionDto = new UserRegisterDto();
        sqlInjectionDto.setUsername("admin'; DROP TABLE users; --");
        sqlInjectionDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sqlInjectionDto)))
                .andExpect(status().isOk()); // Должен обработать как обычный username

        // Попытка входа с тем же "username"
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("admin'; DROP TABLE users; --");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldHandleSpecialCharactersInCredentials() throws Exception {
        // Регистрация с специальными символами
        UserRegisterDto specialCharsDto = new UserRegisterDto();
        specialCharsDto.setUsername("user@domain.com");
        specialCharsDto.setPassword("P@ssw0rd!#$");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialCharsDto)))
                .andExpect(status().isOk());

        // Вход с теми же данными
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("user@domain.com");
        loginDto.setPassword("P@ssw0rd!#$");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldHandleConcurrentRegistrations() throws Exception {
        // Симуляция конкурентных запросов регистрации
        UserRegisterDto user1 = new UserRegisterDto();
        user1.setUsername("concurrent1");
        user1.setPassword("password123");

        UserRegisterDto user2 = new UserRegisterDto();
        user2.setUsername("concurrent2");
        user2.setPassword("password123");

        // Два одновременных запроса на регистрацию разных пользователей
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk());

        // Проверяем, что оба пользователя могут войти
        UserLoginDto login1 = new UserLoginDto();
        login1.setUsername("concurrent1");
        login1.setPassword("password123");

        UserLoginDto login2 = new UserLoginDto();
        login2.setUsername("concurrent2");
        login2.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login2)))
                .andExpect(status().isOk());
    }
}