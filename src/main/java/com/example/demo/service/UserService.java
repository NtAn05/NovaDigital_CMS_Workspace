package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        
        // Auto-assign roles based on username prefix
        if (request.getUsername().toLowerCase().startsWith("admin")) {
            user.setRole("ROLE_ADMIN");
        } else if (request.getUsername().toLowerCase().startsWith("member")) {
            user.setRole("ROLE_MEMBER");
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

    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found with username or email: " + usernameOrEmail));
    }

    /**
     * Find or create user from Google OAuth2 info.
     * - If Google account already exists (provider=GOOGLE, providerId matches) → return existing user.
     * - If email already exists (standard registration) → link Google provider to that account.
     * - If no account exists → auto-create new user (auto-register), no password required.
     */
    public User findOrCreateGoogleUser(String email, String fullName, String avatarUrl, String googleSubjectId) {
        // 1. Find by Google provider + providerId
        java.util.Optional<User> existingByProvider = userRepository.findByProviderAndProviderId("GOOGLE", googleSubjectId);
        if (existingByProvider.isPresent()) {
            User user = existingByProvider.get();
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }

        // 2. Find by email (may have registered via standard form)
        java.util.Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            // Link existing account with Google
            user.setProvider("GOOGLE");
            user.setProviderId(googleSubjectId);
            if (user.getAvatarUrl() == null && avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }

        // 3. Create completely new user (auto-register)
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(fullName != null ? fullName : email.split("@")[0]);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setProvider("GOOGLE");
        newUser.setProviderId(googleSubjectId);
        newUser.setPassword(""); // Google users do not have password, use empty string to avoid DB error
        newUser.setRole("ROLE_USER");
        newUser.setEnabled(true);

        // Generate username from prefix before @ of email, append random numbers if duplicate
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        if (baseUsername.isEmpty()) baseUsername = "user";
        String username = baseUsername;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + (int) (Math.random() * 9000 + 1000);
        }
        newUser.setUsername(username);

        return userRepository.save(newUser);
    }
}
