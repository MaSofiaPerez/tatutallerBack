package com.tatutaller.service;

import com.tatutaller.dto.request.LoginRequest;
import com.tatutaller.dto.request.RegisterRequest;
import com.tatutaller.dto.response.JwtResponse;
import com.tatutaller.entity.User;
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
        
        return new JwtResponse(jwt, user);
    }
    
    public User registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso!");
        }
        
        // Crear nueva cuenta de usuario
        User user = new User(signUpRequest.getName(),
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()));
        
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
            userInfo.put("passwordStartsWith", user.getPassword().substring(0, Math.min(10, user.getPassword().length())));
            result.add(userInfo);
        }
        
        return result;
    }
}
