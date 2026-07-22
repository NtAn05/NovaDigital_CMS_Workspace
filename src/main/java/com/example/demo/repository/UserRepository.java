package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Google OAuth2: Find user by provider (GOOGLE) and providerId (Google Subject ID)
    java.util.Optional<User> findByProviderAndProviderId(String provider, String providerId);

    @Query(value = "SELECT u.* FROM users u LEFT JOIN (SELECT username, MAX(created_at) as max_created_at FROM data_audit_log GROUP BY username) d ON u.username = d.username ORDER BY d.max_created_at DESC", 
           countQuery = "SELECT count(*) FROM users", 
           nativeQuery = true)
    Page<User> findAllUsersSortedByLatestDataAudit(Pageable pageable);
}
