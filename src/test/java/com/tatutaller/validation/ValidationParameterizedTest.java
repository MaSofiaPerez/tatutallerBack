package com.tatutaller.validation;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas parametrizadas específicas para validaciones de DTOs
 * Se enfoca en probar todas las validaciones de Bean Validation
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Pruebas Parametrizadas de Validación")
class ValidationParameterizedTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== PRUEBAS DE VALIDACIÓN PARA LOGIN ==========

    @ParameterizedTest
    @DisplayName("Validación de emails en LoginRequest")
    @MethodSource("emailsValidos")
    void loginRequest_EmailValido_NoDeberiaGenerarViolaciones(String email, String descripcion) {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(email, "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Email válido '" + email + "' no debería generar violaciones: " + descripcion);
    }

    @ParameterizedTest
    @DisplayName("Validación de emails inválidos en LoginRequest")
    @MethodSource("emailsInvalidos")
    void loginRequest_EmailInvalido_DeberiaGenerarViolacion(String email, String descripcion) {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(email, "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Assert
        assertFalse(violations.isEmpty(), "Email inválido '" + email + "' debería generar violaciones: " + descripcion);
        
        boolean tieneViolacionEmail = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(tieneViolacionEmail, "Debería existir una violación específica para el campo email");
    }

    @ParameterizedTest
    @DisplayName("Validación de passwords en LoginRequest")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void loginRequest_PasswordVacio_DeberiaGenerarViolacion(String password) {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@test.com", password);

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Assert
        assertFalse(violations.isEmpty(), "Password vacío o con espacios debería generar violaciones");
        
        boolean tieneViolacionPassword = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(tieneViolacionPassword, "Debería existir una violación específica para el campo password");
    }

    // ========== PRUEBAS DE VALIDACIÓN PARA REGISTRO ==========

    @ParameterizedTest
    @DisplayName("Validación de nombres válidos en RegisterRequest")
    @MethodSource("nombresValidos")
    void registerRequest_NombreValido_NoDeberiaGenerarViolaciones(String nombre, String descripcion) {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(nombre, "user@test.com", "password123");

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Nombre válido '" + nombre + "' no debería generar violaciones: " + descripcion);
    }

    @ParameterizedTest
    @DisplayName("Validación de nombres inválidos en RegisterRequest")
    @MethodSource("nombresInvalidos")
    void registerRequest_NombreInvalido_DeberiaGenerarViolacion(String nombre, String descripcion) {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(nombre, "user@test.com", "password123");

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        assertFalse(violations.isEmpty(), "Nombre inválido '" + nombre + "' debería generar violaciones: " + descripcion);
        
        boolean tieneViolacionNombre = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(tieneViolacionNombre, "Debería existir una violación específica para el campo name");
    }

    @ParameterizedTest
    @DisplayName("Validación de passwords válidos en RegisterRequest")
    @ValueSource(strings = {
            "123456",           // Mínimo válido
            "password123",      // Común
            "MySecurePass!",    // Con símbolos
            "1234567890123456789012345678901234567890", // Muy largo
            "पासवर्ड123",         // Unicode
            "пароль123"         // Cirílico
    })
    void registerRequest_PasswordValido_NoDeberiaGenerarViolaciones(String password) {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "user@test.com", password);

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Password válido '" + password + "' no debería generar violaciones");
    }

    @ParameterizedTest
    @DisplayName("Validación de passwords inválidos en RegisterRequest")
    @MethodSource("passwordsInvalidos")
    void registerRequest_PasswordInvalido_DeberiaGenerarViolacion(String password, String descripcion) {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "user@test.com", password);

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        assertFalse(violations.isEmpty(), "Password inválido debería generar violaciones: " + descripcion);
        
        boolean tieneViolacionPassword = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(tieneViolacionPassword, "Debería existir una violación específica para el campo password");
    }

    @ParameterizedTest
    @DisplayName("Validación completa con diferentes combinaciones")
    @MethodSource("combinacionesValidacion")
    void validacionCompleta_DiversasCombinaciones(
            String nombre, String email, String password, 
            boolean deberiaSerValido, String campoConError, String descripcion) {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(nombre, email, password);

        // Act
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        // Assert
        if (deberiaSerValido) {
            assertTrue(violations.isEmpty(), 
                "Combinación válida no debería generar violaciones: " + descripcion);
        } else {
            assertFalse(violations.isEmpty(), 
                "Combinación inválida debería generar violaciones: " + descripcion);
            
            if (campoConError != null) {
                boolean tieneViolacionEsperada = violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals(campoConError));
                assertTrue(tieneViolacionEsperada, 
                    "Debería existir una violación para el campo: " + campoConError);
            }
        }
    }

    // ========== MÉTODOS DE DATOS PARA PRUEBAS PARAMETRIZADAS ==========

    static Stream<Arguments> emailsValidos() {
        return Stream.of(
                Arguments.of("user@example.com", "Email estándar"),
                Arguments.of("test.email@domain.co.uk", "Email con subdominios"),
                Arguments.of("user+tag@example.com", "Email con etiqueta"),
                Arguments.of("123@example.com", "Email que empieza con números"),
                Arguments.of("very.long.email.address@very.long.domain.name.com", "Email muy largo"),
                Arguments.of("user_name@test-domain.com", "Email con guiones y underscores"),
                Arguments.of("a@b.co", "Email mínimo válido")
        );
    }

    static Stream<Arguments> emailsInvalidos() {
        return Stream.of(
                Arguments.of("", "Email vacío"),
                Arguments.of("   ", "Email solo espacios"),
                Arguments.of("invalid-email", "Sin @"),
                Arguments.of("@domain.com", "Sin parte local"),
                Arguments.of("user@", "Sin dominio"),
                Arguments.of("user.domain.com", "Sin @"),
                Arguments.of("user@@domain.com", "Doble @"),
                Arguments.of("user@domain", "Sin TLD"),
                Arguments.of("user name@domain.com", "Espacio en parte local"),
                Arguments.of("user@domain .com", "Espacio en dominio")
        );
    }

    static Stream<Arguments> nombresValidos() {
        return Stream.of(
                Arguments.of("Juan", "Nombre simple"),
                Arguments.of("María José", "Nombre compuesto"),
                Arguments.of("José Luis García-López", "Nombre con guión"),
                Arguments.of("O'Connor", "Nombre con apostrofe"),
                Arguments.of("李小明", "Nombre en chino"),
                Arguments.of("محمد", "Nombre en árabe"),
                Arguments.of("A", "Nombre de una letra"),
                Arguments.of("A".repeat(100), "Nombre de 100 caracteres (límite)")
        );
    }

    static Stream<Arguments> nombresInvalidos() {
        return Stream.of(
                Arguments.of("", "Nombre vacío"),
                Arguments.of("   ", "Nombre solo espacios"),
                Arguments.of("\t", "Nombre solo tab"),
                Arguments.of("\n", "Nombre solo salto de línea"),
                Arguments.of(null, "Nombre nulo"),
                Arguments.of("A".repeat(101), "Nombre de 101 caracteres (excede límite)")
        );
    }

    static Stream<Arguments> passwordsInvalidos() {
        return Stream.of(
                Arguments.of("", "Password vacío"),
                Arguments.of("   ", "Password solo espacios"),
                Arguments.of("\t\t\t", "Password solo tabs"),
                Arguments.of("12345", "Password de 5 caracteres"),
                Arguments.of("a", "Password de 1 carácter"),
                Arguments.of(null, "Password nulo")
        );
    }

    static Stream<Arguments> combinacionesValidacion() {
        return Stream.of(
                // Casos válidos
                Arguments.of("Juan Pérez", "juan@test.com", "password123", true, null, "Caso completamente válido"),
                Arguments.of("A", "a@b.co", "123456", true, null, "Caso mínimo válido"),
                Arguments.of("A".repeat(100), "long@test.com", "123456", true, null, "Nombre al límite válido"),
                
                // Casos inválidos - nombre
                Arguments.of("", "user@test.com", "password123", false, "name", "Nombre vacío"),
                Arguments.of("A".repeat(101), "user@test.com", "password123", false, "name", "Nombre muy largo"),
                
                // Casos inválidos - email
                Arguments.of("User Test", "invalid-email", "password123", false, "email", "Email inválido"),
                Arguments.of("User Test", "", "password123", false, "email", "Email vacío"),
                
                // Casos inválidos - password
                Arguments.of("User Test", "user@test.com", "12345", false, "password", "Password muy corto"),
                Arguments.of("User Test", "user@test.com", "", false, "password", "Password vacío"),
                
                // Casos múltiples errores
                Arguments.of("", "invalid-email", "123", false, null, "Múltiples errores")
        );
    }
}
