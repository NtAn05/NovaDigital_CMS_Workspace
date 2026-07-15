package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the standalone Resource Allocation account without depending on the
 * sample-data seeder. Existing accounts are never overwritten.
 */
@Component
@Order(2)
public class ResourceManagerAccountSeeder implements CommandLineRunner {

    private static final String RESOURCE_ROLE = "ROLE_RESOURCE";

    @Autowired
    private UserRepository userRepository;

    @Value("${app.resource-manager.username:resource_manager}")
    private String username;

    @Value("${app.resource-manager.password:NovaResource@2026}")
    private String password;

    @Value("${app.resource-manager.full-name:Resource Allocation Manager}")
    private String fullName;

    @Value("${app.resource-manager.email:resource.manager@novadigital.local}")
    private String email;

    @Override
    @Transactional
    public void run(String... args) {
        var existingByUsername = userRepository.findByUsername(username);
        if (existingByUsername.isPresent()) {
            User existing = existingByUsername.get();
            if (!RESOURCE_ROLE.equalsIgnoreCase(existing.getRole())) {
                System.err.println(">>> [ResourceManagerAccountSeeder] Username '" + username
                        + "' already exists with another role. Dedicated resource account was not changed.");
            }
            return;
        }

        if (userRepository.existsByEmail(email)) {
            System.err.println(">>> [ResourceManagerAccountSeeder] Email '" + email
                    + "' already exists. Dedicated resource account was not created.");
            return;
        }

        User manager = new User();
        manager.setUsername(username.trim());
        manager.setPassword(PasswordHasher.hash(password));
        manager.setFullName(fullName.trim());
        manager.setEmail(email.trim());
        manager.setRole(RESOURCE_ROLE);
        manager.setEnabled(true);
        userRepository.save(manager);

        System.out.println(">>> [ResourceManagerAccountSeeder] Dedicated Resource Allocation account created: "
                + username);
    }
}
