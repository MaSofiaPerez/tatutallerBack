package com.tatutaller.controller;

import com.tatutaller.entity.User;
import com.tatutaller.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import com.tatutaller.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.util.Map;

import java.util.Optional;
import com.tatutaller.dto.response.UserProfileResponse;

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

    // GET /api/users/profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOpt.get();
        UserProfileResponse dto = new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name()
        );
        return ResponseEntity.ok(dto);
    }

    // PUT /api/users/profile
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody Map<String, String> body) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOpt.get();

        user.setName(body.getOrDefault("name", user.getName() != null ? user.getName() : ""));
        user.setPhone(body.getOrDefault("phone", user.getPhone() != null ? user.getPhone() : ""));
        user.setAddress(body.getOrDefault("address", user.getAddress() != null ? user.getAddress() : ""));

        userRepository.save(user);

        UserProfileResponse dto = new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name()
        );
        return ResponseEntity.ok(dto);
    }
//Solamente oara Admin debido a que modifica rol
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails) {
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

            UserProfileResponse dto = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getAddress(),
                updatedUser.getRole().name()
            );
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
