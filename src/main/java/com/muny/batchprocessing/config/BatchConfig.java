package com.muny.batchprocessing.config;

import com.muny.batchprocessing.dto.UserInput;
import com.muny.batchprocessing.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    /* @Bean
    public FlatFileItemReader<Users> reader() {
        return new FlatFileItemReaderBuilder<Users>()
                .name("UsersItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names("first_name", "last_name", "username")
                .linesToSkip(1)
                .targetType(Users.class)
                .build();
    }*/

    // 1. Read items each row from csv.
   @Bean
   public FlatFileItemReader<UserInput> reader() {
       return new FlatFileItemReaderBuilder<UserInput>()
               .name("UsersItemReader")
               .resource(new ClassPathResource("data/sample-data.csv"))
               .delimited()
               .names("first_name", "last_name")
               .linesToSkip(1)
               .targetType(UserInput.class)
               .build();
   }
   // 1.1 Read items as multiple file instead using loop.
    @Bean
    public MultiResourceItemReader<UserInput> multiResourceReader(@Value("classpath:data/sample-*.csv") Resource[] resources) {
        return new MultiResourceItemReaderBuilder<UserInput>()
                .name("UsersMultiItemReader")
                .delegate(reader())
                .resources(resources)
                .build();
    }

   // 2. Processing the convert data from UserInput Record to User class.
    @Bean
    public UserProcessor processor() {
        return new UserProcessor();
    }

    // 3. Implement to write to Database
    @Bean
    public JdbcBatchItemWriter<Users> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Users>()
                .sql("INSERT INTO _users (first_name, last_name, username) VALUES (:first_name, :last_name, :username)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    //4. Implement to all from 1 to 3 from read, processing, and write to a step
    @Bean
    public Step step1(JobRepository jobRepository,
                      JpaTransactionManager transactionManager,
                      FlatFileItemReader<UserInput> reader,
                      MultiResourceItemReader<UserInput> multiResourceItemReader,
                      UserProcessor processor,
                      JdbcBatchItemWriter<Users> writer,
                      ChunkLoggingListener chunkLoggingListener) throws Exception {
        return new StepBuilder("step1", jobRepository)
                .<UserInput, Users>chunk(3, transactionManager)
                //.reader(reader)
                .reader(multiResourceItemReader)
                .processor(processor)
                .listener(chunkLoggingListener)
                .writer(writer)
                .build();
    }

    /*
    // -> Read single file configuration. <-
    @Bean
    public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                      FlatFileItemReader<Users> reader, UserProcessor processor, JdbcBatchItemWriter<Users> writer) {
        return new StepBuilder("step1", jobRepository)
                .<Users, Users>chunk(3, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }*/

    // 5. Final Implement by add step to one job to start the process.
    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1, JobCompletionNotificationListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
                .listener(listener)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }
}
