package com.wanted.codebombalms.problems.dataset.application.service;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemDatasetCommandService implements UploadProblemDatasetUseCase {

    private final StoreDatasetFilePort storeDatasetFilePort;
    private final ProblemDatasetPersistencePort problemDatasetPersistencePort;

    @Override
    @Transactional
    public UploadProblemDatasetView handle(UploadProblemDatasetCommand command) {
        validateUploadFile(command);

        StoredDatasetFile storedFile = null;

        try {
            storedFile = storeDatasetFilePort.store(command);
            ProblemDataset dataset = problemDatasetPersistencePort.saveUploadedDataset(storedFile);

            return new UploadProblemDatasetView(
                    dataset.getDatasetId(),
                    dataset.getOriginalFileName(),
                    dataset.getStoredFileName(),
                    dataset.getFileUrl(),
                    dataset.getFilePath(),
                    dataset.getStatus()
            );
        } catch (DomainException e) {
            if (storedFile != null) {
                storeDatasetFilePort.delete(storedFile.getFilePath());
            }

            throw e;
        } catch (Exception e) {
            if (storedFile != null) {
                storeDatasetFilePort.delete(storedFile.getFilePath());
            }

            log.warn("데이터셋 업로드 실패. originalFileName={}", command.originalFileName(), e);
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }

    private void validateUploadFile(UploadProblemDatasetCommand command) {
        if (command == null || command.content() == null || command.content().length == 0) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        String originalFileName = command.originalFileName();

        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".csv")) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
    }
}
