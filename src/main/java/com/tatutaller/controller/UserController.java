package com.tatutaller.controller;

import com.tatutaller.entity.User;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.service.PasswordGeneratorService;
import com.tatutaller.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordGeneratorService passwordGeneratorService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setRole(userDetails.getRole());
            user.setStatus(userDetails.getStatus());

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para obtener solo profesores (para admin)
    @GetMapping("/teachers")
    public ResponseEntity<List<User>> getTeachers() {
        List<User> teachers = userRepository.findByRole(User.Role.TEACHER);
        return ResponseEntity.ok(teachers);
    }

    // Endpoint para crear un nuevo usuario
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User userRequest) {
        try {
            // Verificar si ya existe un usuario con ese email
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().build();
            }

            // Generar contraseña temporal
            String temporaryPassword = passwordGeneratorService.generatePassword();

            // Configurar el usuario
            userRequest.setPassword(passwordEncoder.encode(temporaryPassword));
            userRequest.setMustChangePassword(true);

            // Guardar usuario
            User savedUser = userRepository.save(userRequest);

            // Enviar email con credenciales
            try {
                emailService.sendTemporaryPasswordEmail(
                        savedUser.getEmail(),
                        savedUser.getName(),
                        temporaryPassword);
            } catch (Exception e) {
                System.err.println("Error enviando email: " + e.getMessage());
                // Continuar sin fallar, el usuario ya fue creado
            }

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            System.err.println("Error creando usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
