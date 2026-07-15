package com.erebelo.springloomdemo.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService batchExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(destroyMethod = "close")
    public ExecutorService workerExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
