package com.wanted.codebombalms;

import com.wanted.codebombalms.problems.dataset.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.problems.execution.infrastructure.runner.CloudRunCodeRunnerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        CloudRunCodeRunnerProperties.class,
        GcpStorageProperties.class
})
public class CodeBombaLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeBombaLmsApplication.class, args);
    }

}

