package com.wanted.codebombalms.problems.dataset.infrastructure.storage;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@Slf4j
public class LocalDatasetFileStorage implements StoreDatasetFilePort {

    private static final String UPLOAD_DIR = "uploads/datasets";

    @Override
    public StoredDatasetFile store(UploadProblemDatasetCommand command) throws IOException {
        String originalFileName = command.originalFileName();
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        Path uploadPath = Path.of(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path targetPath = uploadPath.resolve(storedFileName).normalize();
        Files.write(targetPath, command.content());

        String fileUrl = "/" + UPLOAD_DIR + "/" + storedFileName;
        String filePath = UPLOAD_DIR + "/" + storedFileName;

        return StoredDatasetFile.create(
                originalFileName,
                storedFileName,
                fileUrl,
                filePath,
                command.fileSize()
        );
    }

    @Override
    public void delete(String filePath) {
        if(filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            log.warn("로컬 데이터 세트 삭제를 실패했습니다. filePath={}", filePath, e);
        }
    }
}
