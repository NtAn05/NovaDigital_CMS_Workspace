package com.example.demo.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableAspectJAutoProxy
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Cấu hình các tham số lõi cho ThreadPool
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        // Thiết lập hàng đợi (Bounded Queue) để tránh tràn bộ nhớ RAM (OutOfMemory)
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("auditExecutor-");
        // Khi hàng đợi đầy (5000), chính Thread gọi (Caller Thread - Thread tạo sự kiện) sẽ xử lý việc lưu log, tránh mất mát log
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
