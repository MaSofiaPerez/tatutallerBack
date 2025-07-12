package com.tatutaller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.entity.User;
import com.tatutaller.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para los flujos de autenticación completos
 * Estas pruebas verifican el comportamiento end-to-end del sistema de
 * autenticación
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Pruebas de Integración - Autenticación")
class AuthIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(context)
                                .apply(springSecurity())
                                .build();
        }

        // ========== PRUEBAS DE FLUJO COMPLETO ==========

        @Test
        @DisplayName("Flujo completo: Registro exitoso seguido de login exitoso")
        @Transactional
        void flujoCompleto_RegistroYLogin_DeberiaFuncionar() throws Exception {
                // Arrange
                RegisterRequest registerRequest = new RegisterRequest("Integration User", "integration@test.com",
                                "password123");
                registerRequest.setPhone("123456789");
                registerRequest.setAddress("Test Address");

                // Act & Assert - Registro
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente!"))
                                .andExpect(jsonPath("$.user.name").value("Integration User"))
                                .andExpect(jsonPath("$.user.email").value("integration@test.com"));

                // Verificar que el usuario se guardó en la base de datos
                User savedUser = userRepository.findByEmail("integration@test.com").orElse(null);
                assert savedUser != null;
                assert savedUser.getName().equals("Integration User");

                // Act & Assert - Login
                LoginRequest loginRequest = new LoginRequest("integration@test.com", "password123");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.type").value("Bearer"))
                                .andExpect(jsonPath("$.email").value("integration@test.com"))
                                .andExpect(jsonPath("$.name").value("Integration User"));
        }

        @Test
        @DisplayName("Registro duplicado debería fallar y login posterior también")
        @Transactional
        void registroDuplicado_DeberiaFallar() throws Exception {
                // Arrange
                RegisterRequest registerRequest = new RegisterRequest("Duplicate User", "duplicate@test.com",
                                "password123");

                // Primer registro - debería ser exitoso
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // Segundo registro con el mismo email - debería fallar
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error: El email ya está en uso!"));

                // Verificar que solo hay un usuario con ese email
                long count = userRepository.findAll().stream()
                                .mapToLong(user -> user.getEmail().equals("duplicate@test.com") ? 1 : 0)
                                .sum();
                assert count == 1;
        }

        @ParameterizedTest
        @DisplayName("Flujo completo con diferentes tipos de usuarios")
        @MethodSource("usuariosParaPruebas")
        @Transactional
        void flujoCompleto_ConDiferentesTiposUsuarios(String nombre, String email, String password, String descripcion)
                        throws Exception {
                // Arrange
                RegisterRequest registerRequest = new RegisterRequest(nombre, email, password);

                // Act & Assert - Registro
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.user.name").value(nombre))
                                .andExpect(jsonPath("$.user.email").value(email));

                // Act & Assert - Login inmediato
                LoginRequest loginRequest = new LoginRequest(email, password);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.email").value(email))
                                .andExpect(jsonPath("$.name").value(nombre));
        }

        static Stream<Arguments> usuariosParaPruebas() {
                return Stream.of(
                                Arguments.of("Juan Pérez", "juan.perez@test.com", "password123",
                                                "Usuario con nombre normal"),
                                Arguments.of("María José García-López", "maria.garcia@test.com", "securePass456",
                                                "Usuario con nombre compuesto"),
                                Arguments.of("O'Connor Smith", "oconnor@test.com", "mypassword789",
                                                "Usuario con apostrofe"),
                                Arguments.of("李小明", "li.xiaoming@test.com", "chineseuser123",
                                                "Usuario con caracteres chinos"),
                                Arguments.of("Admin", "admin.integration@test.com", "adminPass123",
                                                "Usuario administrador"),
                                Arguments.of("Test Teacher", "teacher.test@test.com", "teacherPass456",
                                                "Usuario profesor"));
        }

        // ========== PRUEBAS DE PERSISTENCIA ==========

        @Test
        @DisplayName("Datos del usuario registrado deben persistir correctamente")
        @Transactional
        void datosUsuario_DebenPersistirCorrectamente() throws Exception {
                // Arrange
                RegisterRequest registerRequest = new RegisterRequest("Persistence Test", "persistence@test.com",
                                "password123");
                registerRequest.setPhone("987654321");
                registerRequest.setAddress("Persistent Address 123");

                // Act - Registro
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // Assert - Verificar persistencia en base de datos
                User savedUser = userRepository.findByEmail("persistence@test.com").orElse(null);
                assert savedUser != null;
                assert savedUser.getName().equals("Persistence Test");
                assert savedUser.getEmail().equals("persistence@test.com");
                assert savedUser.getPhone().equals("987654321");
                assert savedUser.getAddress().equals("Persistent Address 123");
                assert savedUser.getRole() == User.Role.USER; // Rol por defecto
                assert savedUser.getStatus() == User.UserStatus.ACTIVE; // Estado por defecto
                assert savedUser.getCreatedAt() != null;
                assert savedUser.getUpdatedAt() != null;

                // Verificar que la contraseña fue encriptada
                assert !savedUser.getPassword().equals("password123");
                assert passwordEncoder.matches("password123", savedUser.getPassword());
        }

        @Test
        @DisplayName("Login con contraseña incorrecta después de registro exitoso")
        @Transactional
        void loginConPasswordIncorrecta_DeberiaFallar() throws Exception {
                // Arrange - Registro exitoso
                RegisterRequest registerRequest = new RegisterRequest("Wrong Pass User", "wrongpass@test.com",
                                "correctpassword");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // Act & Assert - Login con contraseña incorrecta
                LoginRequest loginRequest = new LoginRequest("wrongpass@test.com", "wrongpassword");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
        }

        @Test
        @DisplayName("Login con usuario inexistente debería fallar")
        void login_ConUsuarioInexistente_DeberiaFallar() throws Exception {
                // Arrange
                LoginRequest loginRequest = new LoginRequest("nonexistent@test.com", "password123");

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
        }

        // ========== PRUEBAS DE VALIDACIÓN AVANZADAS ==========

        @ParameterizedTest
        @DisplayName("Casos límite de validación en registro")
        @MethodSource("casosLimiteRegistro")
        void validacionRegistro_CasosLimite(RegisterRequest request, boolean deberiaSerValido, String descripcion)
                        throws Exception {
                if (deberiaSerValido) {
                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk());
                } else {
                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print()) // Agregar impresión para depuración
                                        .andExpect(deberiaSerValido ? status().isOk() : status().isBadRequest());
                }
        }

        static Stream<Arguments> casosLimiteRegistro() {
                return Stream.of(
                                // Casos válidos límite
                                Arguments.of(new RegisterRequest("A", "a@b.co", "123456"), true,
                                                "Nombre de 1 caracter"),
                                Arguments.of(new RegisterRequest("A".repeat(100), "long@test.com", "123456"), true,
                                                "Nombre de 100 caracteres"),
                                Arguments.of(new RegisterRequest("Test", "test@test.com", "123456"), true,
                                                "Password de 6 caracteres exacto"),

                                // Casos inválidos límite
                                Arguments.of(new RegisterRequest("A".repeat(101), "toolong@test.com", "123456"), false,
                                                "Nombre de 101 caracteres"),
                                Arguments.of(new RegisterRequest("Test", "test@test.com", "12345"), false,
                                                "Password de 5 caracteres"));
        }
}
