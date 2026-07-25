package com.example.demo.repository;

import com.example.demo.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Only save() should be used to insert new records based on immutability design.
    // Updating (save on existing entity ID) or deleting (delete) is not allowed.
}
