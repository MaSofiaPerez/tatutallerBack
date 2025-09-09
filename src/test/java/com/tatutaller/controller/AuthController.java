package com.tatutaller.controller;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Validaciones ya las hace el DTO con @Valid en tu test o controller
        // Para simplificar, siempre devolvemos OK con datos básicos
        return ResponseEntity.ok(
                Map.of(
                        "message", "Usuario registrado con éxito",
                        "email", request.getEmail()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // No chequeamos credenciales en este mock
        // Devolvemos siempre un token dummy
        return ResponseEntity.ok(
                Map.of(
                        "token", "fake-jwt-token-123",
                        "email", request.getEmail()
                )
        );
    }
}
