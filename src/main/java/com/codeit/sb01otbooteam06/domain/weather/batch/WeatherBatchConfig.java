package com.codeit.sb01otbooteam06.domain.weather.batch;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import com.codeit.sb01otbooteam06.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class WeatherBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    private final LocationItemReader locationReader;
    private final WeatherItemWriter itemWriter;

    @Bean
    public Step fetchStep() {
        return new StepBuilder("fetch-step", jobRepository)
            .<Location, Location>chunk(200, tx)       // ↖ chunkSize 조정
            .reader(locationReader)                   // Profile 기반 Reader
            .writer(itemWriter)
            .faultTolerant()
            .retry(Exception.class).retryLimit(3)
            .build();
    }

    @Bean
    public Job weatherChunkJob() {
        return new JobBuilder("weather-chunk-job", jobRepository)
            .incrementer(new RunIdIncrementer())      // 매 실행 고유 ID
            .start(fetchStep())
            .build();
    }
}