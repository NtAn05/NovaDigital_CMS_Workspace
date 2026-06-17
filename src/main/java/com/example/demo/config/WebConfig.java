package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Áp dụng cho tất cả các đường dẫn API bắt đầu bằng /api/
                .allowedOrigins("*") // Cho phép tất cả các Front-end từ mọi nguồn gọi vào (kể cả localhost của Live Server)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Các phương thức HTTP được phép sử dụng
                .allowedHeaders("*"); // Cho phép tất cả các Header gửi lên
    }
}
