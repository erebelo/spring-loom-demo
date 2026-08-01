package com.erebelo.springloomdemo.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadConfig {

    @Bean(destroyMethod = "close")
    ExecutorService virtualThreadBatchExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(destroyMethod = "close")
    ExecutorService virtualThreadWorkerExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
