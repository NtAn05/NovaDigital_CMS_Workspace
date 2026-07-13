package com.example.demo.audit.repository;

import com.example.demo.audit.entity.DataAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface DataAuditLogRepository extends JpaRepository<DataAuditLog, Long> {
    List<DataAuditLog> findAllByOrderByCreatedAtDesc();
    Page<DataAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<DataAuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
