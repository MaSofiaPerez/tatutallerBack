package com.tatutaller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.dto.response.JwtResponse;
import com.tatutaller.entity.User;
import com.tatutaller.service.AuthService;
import com.tatutaller.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para AuthController
 * Incluye pruebas parametrizadas para validaciones de entrada
 */
@WebMvcTest(AuthController.class)
@Import({
    com.tatutaller.config.WebSecurityConfig.class,
    com.tatutaller.security.JwtUtils.class,
    com.tatutaller.security.AuthTokenFilter.class
})
@DisplayName("Pruebas del Controlador de Autenticación")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private com.tatutaller.service.UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.tatutaller.repository.UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== PRUEBAS DE LOGIN ==========

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void login_ConCredencialesValidas_DeberiaRetornarJwtResponse() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@test.com", "password123");
        User mockUser = new User("Test User", "user@test.com", "encodedPassword");
        mockUser.setId(1L);
        JwtResponse mockJwtResponse = new JwtResponse("mocked-jwt-token", mockUser);

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(mockJwtResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"));

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login con credenciales inválidas debería retornar error 400")
    void login_ConCredencialesInvalidas_DeberiaRetornarError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@test.com", "wrongpassword");
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @ParameterizedTest
    @DisplayName("Login con datos inválidos debería retornar error de validación")
    @MethodSource("datosLoginInvalidos")
    void login_ConDatosInvalidos_DeberiaRetornarErrorValidacion(String email, String password, String descripcion) throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // No se debe llamar al servicio si la validación falla
        verify(authService, never()).authenticateUser(any(LoginRequest.class));
    }

    static Stream<Arguments> datosLoginInvalidos() {
        return Stream.of(
                Arguments.of("", "password123", "Email vacío"),
                Arguments.of(null, "password123", "Email nulo"),
                Arguments.of("invalid-email", "password123", "Email sin formato válido"),
                Arguments.of("user@test.com", "", "Password vacío"),
                Arguments.of("user@test.com", null, "Password nulo"),
                Arguments.of("", "", "Email y password vacíos"),
                Arguments.of("not-an-email", "", "Email inválido y password vacío")
        );
    }

    // ========== PRUEBAS DE REGISTRO ==========

    @Test
    @DisplayName("Registro exitoso con datos válidos")
    void register_ConDatosValidos_DeberiaCrearUsuario() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "user@test.com", "password123");
        registerRequest.setPhone("123456789");
        registerRequest.setAddress("Test Address");

        User mockUser = new User("Test User", "user@test.com", "encodedPassword");
        mockUser.setId(1L);
        mockUser.setPhone("123456789");
        mockUser.setAddress("Test Address");

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente!"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("user@test.com"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
        verify(emailService, times(1)).sendWelcomeEmail("user@test.com", "Test User");
    }

    @Test
    @DisplayName("Registro con email duplicado debería retornar error")
    void register_ConEmailDuplicado_DeberiaRetornarError() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "existing@test.com", "password123");

        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Error: El email ya está en uso!"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: El email ya está en uso!"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Registro exitoso aunque falle el envío de email")
    void register_ConErrorEnEmail_DeberiaCompletarRegistro() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "user@test.com", "password123");

        User mockUser = new User("Test User", "user@test.com", "encodedPassword");
        mockUser.setId(1L);

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockUser);
        doThrow(new RuntimeException("Error de email")).when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente!"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
        verify(emailService, times(1)).sendWelcomeEmail("user@test.com", "Test User");
    }

    @ParameterizedTest
    @DisplayName("Registro con datos inválidos debería retornar error de validación")
    @MethodSource("datosRegistroInvalidos")
    void register_ConDatosInvalidos_DeberiaRetornarErrorValidacion(
            String name, String email, String password, String descripcion) throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // No se debe llamar al servicio si la validación falla
        verify(authService, never()).registerUser(any(RegisterRequest.class));
    }

    static Stream<Arguments> datosRegistroInvalidos() {
        return Stream.of(
                Arguments.of("", "user@test.com", "password123", "Nombre vacío"),
                Arguments.of(null, "user@test.com", "password123", "Nombre nulo"),
                Arguments.of("Test User", "", "password123", "Email vacío"),
                Arguments.of("Test User", null, "password123", "Email nulo"),
                Arguments.of("Test User", "invalid-email", "password123", "Email inválido"),
                Arguments.of("Test User", "user@test.com", "", "Password vacío"),
                Arguments.of("Test User", "user@test.com", null, "Password nulo"),
                Arguments.of("Test User", "user@test.com", "123", "Password muy corto"),
                Arguments.of("A".repeat(101), "user@test.com", "password123", "Nombre muy largo")
        );
    }

    @ParameterizedTest
    @DisplayName("Registro con passwords de diferentes longitudes")
    @ValueSource(strings = {"123456", "1234567", "12345678901234567890", "password!@#$%^&*()"})
    void register_ConDiferentesPasswords_DeberiaValidarCorrectamente(String password) throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "user@test.com", password);
        User mockUser = new User("Test User", "user@test.com", "encodedPassword");
        mockUser.setId(1L);

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Act & Assert - Todos estos passwords deberían ser válidos (>= 6 caracteres)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente!"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }
}
