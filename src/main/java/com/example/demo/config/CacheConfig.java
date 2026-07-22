package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Added code for Cache feature
/**
 * Enables Spring In-memory Cache using ConcurrentMapCacheManager (JDK built-in).
 * No additional dependency required in pom.xml.
 *
 * FIX: Spring Boot 4.x does not automatically create CacheManager if no
 * cache provider (Redis, Ehcache...) is present on classpath.
 * Solution: Explicitly declare ConcurrentMapCacheManager bean.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Added code for Cache feature
    /**
     * Register In-memory CacheManager with cache name "vacancies".
     * Add new cache names here when extending to other features.
     */
    @Bean
    public CacheManager cacheManager() {
        // ConcurrentMapCacheManager uses ConcurrentHashMap — thread-safe,
        // no additional configuration needed, suitable for single-server environment.
        return new ConcurrentMapCacheManager("vacancies");
    }
}
