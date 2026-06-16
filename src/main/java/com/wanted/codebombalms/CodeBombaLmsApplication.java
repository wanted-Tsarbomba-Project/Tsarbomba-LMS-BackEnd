package com.wanted.codebombalms;

import com.wanted.codebombalms.global.infrastructure.storage.GcpStorageProperties;
import com.wanted.codebombalms.problems.execution.infrastructure.config.CodeExecutionProperties;
import com.wanted.codebombalms.problems.execution.infrastructure.runner.CloudRunCodeRunnerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        CloudRunCodeRunnerProperties.class,
        GcpStorageProperties.class,
        CodeExecutionProperties.class
})
public class CodeBombaLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeBombaLmsApplication.class, args);
    }

}

