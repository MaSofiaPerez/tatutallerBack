package com.tatutaller.controller;

import com.tatutaller.entity.User;
import com.tatutaller.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('USER', 'TEACHER', 'ADMIN')")
public class UserInfoController {

    @Autowired
    private UserRepository userRepository;

    // Endpoint privado para obtener datos de usuario por ID (para usuarios
    // autenticados)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
