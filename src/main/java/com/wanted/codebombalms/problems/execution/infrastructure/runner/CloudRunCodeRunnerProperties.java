package com.wanted.codebombalms.problems.execution.infrastructure.runner;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "code-runner.cloud-run")
public class CloudRunCodeRunnerProperties {

    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
