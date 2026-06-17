package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordHasher.hash(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        // Auto-assign admin role if username starts with or equals admin
        if (request.getUsername().toLowerCase().startsWith("admin")) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }
        
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User authenticateUser(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Incorrect username or email"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }



        if (!PasswordHasher.verify(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        // Update last login time
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }
}
