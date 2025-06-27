package com.tatutaller.controller;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.dto.response.JwtResponse;
import com.tatutaller.entity.User;
import com.tatutaller.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthService authService;
    
    @Autowired
    private com.tatutaller.service.EmailService emailService;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Credenciales inválidas");
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
}
