package com.wanted.codebombalms.domain.problems.dataset.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DatasetFileStorage {
    StoredDatasetFile store(MultipartFile file) throws IOException;
}
