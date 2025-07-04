package com.tatutaller.controller;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.dto.request.ChangePasswordRequest;
import com.tatutaller.dto.response.JwtResponse;
import com.tatutaller.entity.User;
import com.tatutaller.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS })
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    private com.tatutaller.service.EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("=== RECIBIDA PETICIÓN POST LOGIN ===");
        System.out.println("Email recibido: '" + loginRequest.getEmail() + "'");
        System.out.println("Password recibido: '" + loginRequest.getPassword() + "'");
        System.out.println("=======================================");

        try {
            System.out.println("Intento de login para email: " + loginRequest.getEmail());
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            System.out.println("Login exitoso para: " + loginRequest.getEmail());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            System.err.println("Error en login para " + loginRequest.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "Credenciales inválidas");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            User user = authService.registerUser(signUpRequest);

            // Enviar email de bienvenida
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (Exception e) {
                // Log error but don't fail the registration
                System.err.println("Error enviando email de bienvenida: " + e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente!");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Get the current user's email from the security context
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Usuario no autenticado");
                return ResponseEntity.status(401).body(error);
            }

            String email = authentication.getName();

            // Get the user to check if they must change password
            User user = authService.getUserByEmail(email);

            if (user.getMustChangePassword() != null && user.getMustChangePassword()) {
                // For users who must change password (first time), no current password
                // validation needed
                authService.changePassword(email, request.getNewPassword());
            } else {
                // For regular password changes, validate current password
                if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "La contraseña actual es obligatoria para cambios de contraseña regulares");
                    return ResponseEntity.badRequest().body(error);
                }
                authService.changePasswordWithValidation(email, request.getCurrentPassword(), request.getNewPassword());
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Endpoint temporal para debugging
    @GetMapping("/debug/users")
    public ResponseEntity<?> debugUsers() {
        try {
            return ResponseEntity.ok(authService.getAllUsersForDebug());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
