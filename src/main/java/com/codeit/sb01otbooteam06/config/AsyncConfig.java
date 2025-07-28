package com.codeit.sb01otbooteam06.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Bean(name = "eventTaskExecutor")
  public Executor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4); // 기본 스레드 수
    executor.setMaxPoolSize(10); // 최대 스레드 수
    executor.setQueueCapacity(100); // 큐 용량
    executor.setThreadNamePrefix("event-task-");
    executor.initialize();
    return executor;
  }

  @Override
  public Executor getAsyncExecutor() {
    return eventTaskExecutor();
  }
}