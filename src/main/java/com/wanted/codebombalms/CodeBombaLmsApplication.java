package com.wanted.codebombalms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeBombaLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeBombaLmsApplication.class, args);
    }

}
