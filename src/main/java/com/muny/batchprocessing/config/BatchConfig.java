package com.muny.batchprocessing.config;

import com.muny.batchprocessing.entity.Users;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import javax.sql.DataSource;

@Configuration
public class BatchConfig {
    
    @Bean
    public FlatFileItemReader<Users> reader() {
        return new FlatFileItemReaderBuilder<Users>()
                .name("UsersItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names("first_name", "last_name", "username")
                .linesToSkip(1)
                .targetType(Users.class)
                .build();
    }

    @Bean
    public UserProcessor processor() {
        return new UserProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Users> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Users>()
                .sql("INSERT INTO _users (first_name, last_name, username) VALUES (:first_name, :last_name, :username)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1, JobCompletionNotificationListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                      FlatFileItemReader<Users> reader, UserProcessor processor, JdbcBatchItemWriter<Users> writer) {
        return new StepBuilder("step1", jobRepository)
                .<Users, Users>chunk(3, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
