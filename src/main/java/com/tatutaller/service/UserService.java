package com.tatutaller.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tatutaller.entity.User;
import com.tatutaller.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public List<User> findTeachers() {
        return userRepository.findByRole(User.Role.TEACHER);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

}
