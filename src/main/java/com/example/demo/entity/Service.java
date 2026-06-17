package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "services")
@Data // Tự động tạo getter, setter, equals, hashCode, toString
@NoArgsConstructor // Tự động tạo constructor không tham số
@AllArgsConstructor // Tự động tạo constructor có đầy đủ tham số
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl; // Đường dẫn tới icon/image của dịch vụ

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Tự động chèn thời gian hiện tại khi dữ liệu được tạo mới
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
