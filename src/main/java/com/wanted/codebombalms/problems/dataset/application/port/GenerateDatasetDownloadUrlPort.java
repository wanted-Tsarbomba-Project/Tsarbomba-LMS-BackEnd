package com.wanted.codebombalms.problems.dataset.application.port;

public interface GenerateDatasetDownloadUrlPort {

    String generate(String filePath, String originalFileName);
}
