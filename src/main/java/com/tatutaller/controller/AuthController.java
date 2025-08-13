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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.http.HttpStatus;

import java.util.Collections;
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

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAuth() {
        // Obtener el usuario autenticado
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuario no autenticado"));
        }

        String email = authentication.getName();
        User user = authService.getUserByEmail(email);

        // Crear y devolver JwtResponse
        JwtResponse jwtResponse = new JwtResponse(null, user); // Token es null porque no se genera aquí
        return ResponseEntity.ok(jwtResponse);
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

    @PostMapping("/login-google")
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String, String> body) {
        try {
            String idTokenString = body.get("token");
            System.out.println("Antes de crear GoogleIdTokenVerifier");
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList("737455985650-mdp35pj783ms660iu8c3tvc63pl13hga.apps.googleusercontent.com"))
                    .build();
            System.out.println("Después de crear GoogleIdTokenVerifier");

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String nombre = (String) payload.get("name");

                // Busca o crea el usuario en tu sistema
                User user = authService.loginWithGoogle(email, nombre);

                // Genera tu propio JWT
                String jwt = authService.generateJwtForUser(user);

                Map<String, Object> response = new HashMap<>();
                response.put("token", jwt);
                response.put("user", user);

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Token de Google inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            e.printStackTrace(); // <-- Agrega esto para ver el error real
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}
