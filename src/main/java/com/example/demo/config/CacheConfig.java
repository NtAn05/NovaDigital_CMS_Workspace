package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Bổ sung code cho tính năng Cache
/**
 * Bật Spring In-memory Cache dùng ConcurrentMapCacheManager (JDK built-in).
 * Không cần thêm dependency vào pom.xml.
 *
 * FIX: Spring Boot 4.x không tự động tạo CacheManager nếu không có
 * cache provider nào (Redis, Ehcache…) trên classpath.
 * Giải pháp: khai báo tường minh bean ConcurrentMapCacheManager.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Bổ sung code cho tính năng Cache
    /**
     * Đăng ký In-memory CacheManager với tên cache "vacancies".
     * Thêm tên cache mới vào đây khi mở rộng sang các tính năng khác.
     */
    @Bean
    public CacheManager cacheManager() {
        // ConcurrentMapCacheManager dùng ConcurrentHashMap — thread-safe,
        // không cần cấu hình thêm, phù hợp cho môi trường đơn server.
        return new ConcurrentMapCacheManager("vacancies");
    }
}
