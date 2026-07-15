package com.example.demo.audit.repository;

import com.example.demo.audit.entity.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AuthLogRepository extends JpaRepository<AuthLog, Long> {
    List<AuthLog> findAllByOrderByCreatedAtDesc();
    Page<AuthLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
