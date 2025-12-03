package com.neu.finalproject.meskot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Defines a dedicated thread pool for CPU-intensive encoding tasks.
     * We give it a small, fixed size so it doesn't overwhelm the system,
     * and a large queue to hold pending uploads.
     */
    @Bean(name = "encodingTaskExecutor")
    public Executor encodingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Set core/max pool size based on your server's CPU cores
        // e.g., for a 4-core server, you might use 2 threads for encoding
        int coreCount = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(Math.max(2, coreCount / 2));
        executor.setMaxPoolSize(Math.max(2, coreCount / 2));
        executor.setQueueCapacity(100); // 100 pending uploads
        executor.setThreadNamePrefix("Encoding-");
        executor.initialize();
        return executor;
    }
}