package com.muny.batchprocessing.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

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
}
