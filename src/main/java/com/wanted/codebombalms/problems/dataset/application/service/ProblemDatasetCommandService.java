package com.wanted.codebombalms.problems.dataset.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.application.usecase.ConnectProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionRequest;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionTarget;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemDatasetCommandService implements
        ConnectProblemDatasetUseCase,
        UploadProblemDatasetUseCase {

    private final StoreDatasetFilePort storeDatasetFilePort;
    private final ProblemDatasetPersistencePort problemDatasetPersistencePort;

    @Override
    @Transactional
    public ConnectProblemDatasetView handle(ConnectProblemDatasetCommand command) {
        ProblemDatasetConnectionRequest request =
                ProblemDatasetConnectionRequest.of(command.problemSetId(), command.datasetId());

        ProblemDatasetConnectionTarget target = problemDatasetPersistencePort.loadConnectionTarget(request);
        target.validateConnectable();

        ProblemDatasetConnection connection =
                problemDatasetPersistencePort.connectDataset(request);

        return new ConnectProblemDatasetView(
                connection.getProblemSetId(),
                connection.getDatasetId(),
                startCode(connection.getFileUrl())
        );
    }

    @Override
    @Transactional
    public UploadProblemDatasetView handle(UploadProblemDatasetCommand command) {
        validateUploadFile(command);

        StoredDatasetFile storedFile = null;

        try {
            storedFile = storeDatasetFilePort.store(command);
            ProblemDataset dataset =
                    problemDatasetPersistencePort.saveUploadedDataset(storedFile);

            return new UploadProblemDatasetView(
                    dataset.getDatasetId(),
                    dataset.getOriginalFileName(),
                    dataset.getStoredFileName(),
                    dataset.getFileUrl(),
                    dataset.getFilePath(),
                    dataset.getStatus()
            );
        } catch (Exception e) {
            if (storedFile != null) {
                storeDatasetFilePort.delete(storedFile.getFilePath());
            }

            log.warn("Dataset upload failed. originalFileName={}", command.originalFileName(), e);
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

    private String startCode(String fileUrl) {
        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + fileUrl + "\")";
    }
}
