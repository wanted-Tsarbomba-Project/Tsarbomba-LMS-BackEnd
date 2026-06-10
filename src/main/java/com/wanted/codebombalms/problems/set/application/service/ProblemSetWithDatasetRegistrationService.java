package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetWithDatasetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemSetWithDatasetRegistrationService implements RegisterProblemSetWithDatasetUseCase {

    private final RegisterProblemSetUseCase registerProblemSetUseCase;
    private final StoreDatasetFilePort storeDatasetFilePort;
    private final ProblemDatasetPersistencePort problemDatasetPersistencePort;
    private static final long MAX_DATASET_FILE_SIZE = 10 * 1024 * 1024;
    private static final String CSV_EXTENSION = ".csv";

    @Override
    @Transactional
    public ProblemSetWithDatasetCreateView handle(
            RegisterProblemSetCommand problemSetCommand,
            UploadProblemDatasetCommand datasetCommand
    ) {
        validateDatasetFile(datasetCommand);

        var problemSet = registerProblemSetUseCase.handle(problemSetCommand);

        StoredDatasetFile storedFile = null;

        try {
            storedFile = storeDatasetFilePort.store(datasetCommand);

            ProblemDataset dataset = problemDatasetPersistencePort.saveUploadedDataset(
                    problemSet.problemSetId(),
                    storedFile
            );

            return new ProblemSetWithDatasetCreateView(
                    problemSet.problemSetId(),
                    dataset.getDatasetId(),
                    problemSet.title(),
                    problemSet.categoryName(),
                    problemSet.totalProblemCount(),
                    problemSet.createdProblemCount(),
                    problemSet.createdTestCaseCount(),
                    dataset.getOriginalFileName(),
                    dataset.getFileUrl(),
                    startCode(dataset.getFileUrl())
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

            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }

    private void validateDatasetFile(UploadProblemDatasetCommand command) {
        if (command == null || command.content() == null || command.content().length == 0) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        String originalFileName = command.originalFileName();

        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(CSV_EXTENSION)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        if (command.fileSize() <= 0 || command.fileSize() > MAX_DATASET_FILE_SIZE) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        String contentType = command.contentType();

        if (contentType != null
                && !contentType.equals("text/csv")
                && !contentType.equals("application/vnd.ms-excel")) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
    }

    private String startCode(String fileUrl) {
        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + fileUrl + "\")";
    }
}
