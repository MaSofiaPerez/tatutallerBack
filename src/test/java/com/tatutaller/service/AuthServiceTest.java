package com.tatutaller.service;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.dto.response.JwtResponse;
import com.tatutaller.entity.User;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AuthService
 * Incluye pruebas parametrizadas para diferentes escenarios de autenticación y
 * registro
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Autenticación")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "user@test.com", "encodedPassword");
        testUser.setId(1L);

        loginRequest = new LoginRequest("user@test.com", "password123");
        registerRequest = new RegisterRequest("Test User", "user@test.com", "password123");
    }

    // ========== PRUEBAS DE AUTENTICACIÓN ==========

    @Test
    @DisplayName("Autenticación exitosa debería retornar JWT response")
    void authenticateUser_ConCredencialesValidas_DeberiaRetornarJwtResponse() {
        // Arrange
        String expectedToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(expectedToken);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

        // Act
        JwtResponse result = authService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
        verify(userRepository, times(1)).findByEmail("user@test.com");
    }

    @Test
    @DisplayName("Autenticación con credenciales inválidas debería lanzar excepción")
    void authenticateUser_ConCredencialesInvalidas_DeberiaLanzarExcepcion() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateJwtToken(any());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Autenticación exitosa pero usuario no encontrado debería lanzar excepción")
    void authenticateUser_UsuarioNoEncontrado_DeberiaLanzarExcepcion() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("token");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("user@test.com");
    }

    @ParameterizedTest
    @DisplayName("Autenticación con diferentes tipos de usuarios")
    @MethodSource("tiposDeUsuario")
    void authenticateUser_ConDiferentesTiposUsuario_DeberiaAutenticarCorrectamente(
            User.Role role, User.UserStatus status, String descripcion) {
        // Arrange
        testUser.setRole(role);
        testUser.setStatus(status);
        String expectedToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(expectedToken);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

        // Act
        JwtResponse result = authService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals(role.toString(), result.getRole());
        assertEquals(testUser.getId(), result.getId());
    }

    static Stream<Arguments> tiposDeUsuario() {
        return Stream.of(
                Arguments.of(User.Role.USER, User.UserStatus.ACTIVE, "Usuario activo normal"),
                Arguments.of(User.Role.ADMIN, User.UserStatus.ACTIVE, "Administrador activo"),
                Arguments.of(User.Role.TEACHER, User.UserStatus.ACTIVE, "Profesor activo"),
                Arguments.of(User.Role.USER, User.UserStatus.INACTIVE, "Usuario inactivo"),
                Arguments.of(User.Role.ADMIN, User.UserStatus.INACTIVE, "Admin inactivo"));
    }

    // ========== PRUEBAS DE REGISTRO ==========

    @Test
    @DisplayName("Registro exitoso debería crear y guardar usuario")
    void registerUser_ConDatosValidos_DeberiaCrearUsuario() {
        // Arrange
        String encodedPassword = "encodedPassword123";
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmail("user@test.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Registro con email existente debería lanzar excepción")
    void registerUser_ConEmailExistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });

        assertEquals("Error: El email ya está en uso!", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("user@test.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registro con datos opcionales debería incluirlos en el usuario")
    void registerUser_ConDatosOpcionales_DeberiaIncluirlos() {
        // Arrange
        registerRequest.setPhone("123456789");
        registerRequest.setAddress("Test Address 123");

        User userConDatos = new User("Test User", "user@test.com", "encodedPassword");
        userConDatos.setPhone("123456789");
        userConDatos.setAddress("Test Address 123");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userConDatos);

        // Act
        User result = authService.registerUser(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("123456789", result.getPhone());
        assertEquals("Test Address 123", result.getAddress());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @ParameterizedTest
    @DisplayName("Registro con diferentes nombres válidos")
    @ValueSource(strings = {
            "Juan",
            "María García",
            "José Luis Rodríguez",
            "Ana-Sofía",
            "O'Connor",
            "李小明",
            "محمد"
    })
    void registerUser_ConDiferentesNombres_DeberiaCrearUsuarioCorrectamente(String nombre) {
        // Arrange
        RegisterRequest requestConNombre = new RegisterRequest(nombre, "user@test.com", "password123");
        User userConNombre = new User(nombre, "user@test.com", "encodedPassword");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userConNombre);

        // Act
        User result = authService.registerUser(requestConNombre);

        // Assert
        assertNotNull(result);
        assertEquals(nombre, result.getName());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @ParameterizedTest
    @DisplayName("Registro con diferentes emails válidos")
    @MethodSource("emailsValidos")
    void registerUser_ConDiferentesEmails_DeberiaCrearUsuarioCorrectamente(String email, String descripcion) {
        // Arrange
        RegisterRequest requestConEmail = new RegisterRequest("Test User", email, "password123");
        User userConEmail = new User("Test User", email, "encodedPassword");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userConEmail);

        // Act
        User result = authService.registerUser(requestConEmail);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    static Stream<Arguments> emailsValidos() {
        return Stream.of(
                Arguments.of("simple@example.com", "Email simple"),
                Arguments.of("very.long.email.address@very.long.domain.name.com", "Email largo"),
                Arguments.of("user+tag@example.com", "Email con +"),
                Arguments.of("user.name+tag@example.co.uk", "Email con subdominios"),
                Arguments.of("123@example.com", "Email que empieza con números"),
                Arguments.of("test_email@test-domain.com", "Email con guiones y underscores"));
    }

    @ParameterizedTest
    @DisplayName("Registro con diferentes longitudes de contraseña")
    @ValueSource(strings = { "123456", "1234567890", "password123", "very_long_password_123456789" })
    void registerUser_ConDiferentesContrasenas_DeberiaEncriptarCorrectamente(String password) {
        // Arrange
        RegisterRequest requestConPassword = new RegisterRequest("Test User", "user@test.com", password);
        String encodedPassword = "encoded_" + password;

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.registerUser(requestConPassword);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ========== PRUEBAS DE INTEGRACIÓN DEL FLUJO COMPLETO ==========

    @Test
    @DisplayName("Flujo completo: Registro seguido de autenticación")
    void flujoCompleto_RegistroYAutenticacion_DeberiaFuncionar() {
        // Arrange - Registro
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act - Registro
        User registeredUser = authService.registerUser(registerRequest);

        // Assert - Registro
        assertNotNull(registeredUser);
        assertEquals("Test User", registeredUser.getName());

        // Arrange - Autenticación
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

        // Act - Autenticación
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        // Assert - Autenticación
        assertNotNull(jwtResponse);
        assertEquals("jwt-token", jwtResponse.getToken());
        assertEquals(testUser.getId(), jwtResponse.getId());

        // Verificaciones finales
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByEmail("user@test.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
    }
}
