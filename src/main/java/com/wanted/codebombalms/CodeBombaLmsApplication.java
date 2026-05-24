package com.wanted.codebombalms;

import com.wanted.codebombalms.problems.execution.infrastructure.runner.CodeRunnerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(CodeRunnerProperties.class)
@SpringBootApplication
public class CodeBombaLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeBombaLmsApplication.class, args);
    }

}
