package com.example.demo.audit.repository;

import com.example.demo.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Chỉ nên sử dụng save() để thêm mới theo tính bất biến của thiết kế.
    // Việc cập nhật (save trên entity đã có ID) hoặc xóa (delete) không được gọi.
}
