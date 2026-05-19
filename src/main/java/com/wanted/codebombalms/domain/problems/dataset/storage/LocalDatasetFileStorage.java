package com.wanted.codebombalms.domain.problems.dataset.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalDatasetFileStorage implements DatasetFileStorage {

    private static final String UPLOAD_DIR = "uploads/datasets";

    @Override
    public StoredDatasetFile store(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        Path uploadPath = Path.of(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        Path targetPath = uploadPath.resolve(storedFileName);
        file.transferTo(targetPath);

        String fileUrl = "/" + UPLOAD_DIR + "/" + storedFileName;
        String filePath = "data/" + originalFileName;

        return new StoredDatasetFile(
                originalFileName,
                storedFileName,
                fileUrl,
                filePath,
                file.getSize()
        );
    }
}