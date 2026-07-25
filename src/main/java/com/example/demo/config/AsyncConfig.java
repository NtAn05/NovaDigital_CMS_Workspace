package com.example.demo.config;

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
        // Configure core parameters for ThreadPool
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        // Set up bounded queue to prevent RAM overflow (OutOfMemory)
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("auditExecutor-");
        // When queue is full (5000), the Caller Thread (event creator) handles log persistence to prevent log loss
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
