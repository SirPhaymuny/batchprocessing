# How to implement Spring Batch
## 📦 2. Required Dependencies (Maven)

```xml
<!-- Spring Batch Starter (includes core + Spring Boot auto-configuration) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>

<!-- Required for Spring Batch to work (transaction management) if jpa no need this-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```
## ⚙️ 3. Basic Configuration
### Configuration file 
```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // 1. Read items each row from csv.
    @Bean
    public FlatFileItemReader<UserInput> reader() {
        return new FlatFileItemReaderBuilder<UserInput>()
            //***Set FlatFileItemReaderBuilder***
        
    }

    // 1.1 Read items as multiple file instead using loop.
    @Bean
    public MultiResourceItemReader<UserInput> multiResourceReader(@Value("classpath:data/sample-*.csv") Resource[] resources) {
        return new MultiResourceItemReaderBuilder<UserInput>()
         //***Set MultiResourceItemReader***
    }

    // 2. Processing the convert data from UserInput Record to User class.
    @Bean
    public UserProcessor processor() {
        return new UserProcessor();
    }

    // 3. Implement to writer to Database
    @Bean
    public JdbcBatchItemWriter<Class> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Class>()
        //***Set JdbcBatchItemWriter***
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
        //***Set StepBuilder***
    }
    
    // 5. Final Implement by add step to one job to start the process.
    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1, JobCompletionNotificationListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
        //***Set JobBuilder***    
    }
}
``` 
### Processor Implement
```java
public class UserProcessor implements ItemProcessor<FromClass, ToClass> {
    @Override
    public ToClass process(FromClass user) throws Exception {
        //*** Set Business Logic***
        return users;
    }
}
```
### Job Notification 
```java
@Component
public class JobCompletionNotificationListener implements JobExecutionListener {
    private final JdbcTemplate jdbcTemplate;
    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            jdbcTemplate
                    .query("SELECT first_name, last_name FROM _users", new DataClassRowMapper<>(Users.class))
                    .forEach(person -> log.info("Found <{}> in the database.", person));
        }
    }
}
```
## 🏃‍♂️Run it Through Api
```java
private final JobLauncher jobLauncher;
private final Job importUserJob;
private final JobExplorer jobExplorer;

public UserController(JobLauncher jobLauncher, Job importUserJob, JobExplorer jobExplorer) {
    this.jobLauncher = jobLauncher;
    this.importUserJob = importUserJob;
    this.jobExplorer = jobExplorer;
}

@PostMapping("/import-users")
public ResponseEntity<String> launchImportUserJob() throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
            .addLong("startAt", System.currentTimeMillis())
            .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(importUserJob, jobParameters);
    return ResponseEntity.ok()
            .body("Job started with ID: " + jobExecution.getId());
}
```

📄 _Created by Sir Phaymuny_