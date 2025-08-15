package com.tatutaller.service;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.dto.response.JwtResponse;
import com.tatutaller.entity.User;
import com.tatutaller.entity.User.Role;
import com.tatutaller.repository.UserRepository;
import com.tatutaller.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Incluir todos los datos relevantes del usuario en la respuesta
        return new JwtResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress(),
                user.getRole().name(), user.getMustChangePassword());
    }

    public User registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso!");
        }

        // Crear nueva cuenta de usuario
        User user = new User(signUpRequest.getName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.CLIENTE);
        user.setPhone(signUpRequest.getPhone());
        user.setAddress(signUpRequest.getAddress());

        return userRepository.save(user);
    }

    // Método temporal para debugging
    public java.util.List<java.util.Map<String, Object>> getAllUsersForDebug() {
        java.util.List<User> users = userRepository.findAll();
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

        for (User user : users) {
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("passwordStartsWith",
                    user.getPassword().substring(0, Math.min(10, user.getPassword().length())));
            result.add(userInfo);
        }

        return result;
    }

    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(encoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    public void changePasswordWithValidation(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validate current password
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Login o registro de usuario usando Google.
     * Si el usuario existe, lo retorna. Si no existe, lanza excepción.
     */
    public User loginWithGoogle(String email, String nombre) {
        String emailNormalizado = email.trim().toLowerCase();
        return userRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new RuntimeException(
                        "El usuario no está registrado en el sistema. Solicite acceso al administrador."));
    }

    /**
     * Genera un JWT para el usuario autenticado (para login con Google).
     */
    public String generateJwtForUser(User user) {
        // Usa tu clase UserPrincipal personalizada
        com.tatutaller.security.UserPrincipal userPrincipal = com.tatutaller.security.UserPrincipal.create(user);
        org.springframework.security.core.Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        return jwtUtils.generateJwtToken(authentication);
    }
}
