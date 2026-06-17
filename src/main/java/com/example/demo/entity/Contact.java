package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String title; // Tiêu đề lời nhắn (Message title)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Nội dung chi tiết (Detailed content)

    @Column(length = 50)
    private String status = "PENDING"; // Trạng thái xử lý: PENDING, DONE...

    @Column(columnDefinition = "TEXT")
    private String reply; // Câu trả lời từ member

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt; // Thời gian trả lời

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}
