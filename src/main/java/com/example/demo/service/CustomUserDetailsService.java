package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));

        String role = user.getRole();

        // Normalize role for Spring Security
        if (role != null) {

            // TEAM_MEMBER, Team_Member or ROLE_MEMBER -> ROLE_MEMBER
            if ("TEAM_MEMBER".equalsIgnoreCase(role)
                    || "Team_Member".equalsIgnoreCase(role)
                    || "ROLE_MEMBER".equalsIgnoreCase(role)) {

                role = "ROLE_MEMBER";
            }
            // Other roles without ROLE_ prefix
            else if (!role.startsWith("ROLE_")) {

                role = "ROLE_" + role.toUpperCase();
            }

        } else {
            // Case when role is null
            role = "ROLE_MEMBER";
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                (user.getPassword() != null && !user.getPassword().isEmpty()) ? user.getPassword() : "", // Google OAuth2 users do not have password
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}