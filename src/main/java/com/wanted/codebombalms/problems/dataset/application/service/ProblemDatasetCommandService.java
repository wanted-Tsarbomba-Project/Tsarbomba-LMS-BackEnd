package com.wanted.codebombalms.problems.dataset.application.service;

import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.application.policy.ProblemDatasetConnectionPolicy;
import com.wanted.codebombalms.problems.dataset.application.policy.ProblemDatasetFileValidationPolicy;
import com.wanted.codebombalms.problems.dataset.application.port.StoreDatasetFilePort;
import com.wanted.codebombalms.problems.dataset.application.usecase.ConnectProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionRequest;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.dataset.domain.repository.ProblemDatasetRepository;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
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

    private final ProblemDatasetFileValidationPolicy validationPolicy;
    private final ProblemDatasetConnectionPolicy connectionPolicy;
    private final StoreDatasetFilePort storeDatasetFilePort;
    private final ProblemDatasetRepository problemDatasetRepository;

    @Override
    @Transactional
    public ConnectProblemDatasetView handle(ConnectProblemDatasetCommand command) {
        ProblemDatasetConnectionRequest request =
                ProblemDatasetConnectionRequest.of(command.problemId(), command.datasetId());

        connectionPolicy.validate(problemDatasetRepository.loadConnectionTarget(request));
        ProblemDatasetConnection connection = problemDatasetRepository.connectDataset(request);

        return new ConnectProblemDatasetView(
                connection.getProblemId(),
                connection.getDatasetId(),
                startCode(connection.getFilePath())
        );
    }

    @Override
    @Transactional
    public UploadProblemDatasetView handle(UploadProblemDatasetCommand command) {
        validationPolicy.validate(command);

        try {
            StoredDatasetFile storedFile = storeDatasetFilePort.store(command);
            ProblemDataset dataset = problemDatasetRepository.saveUploadedDataset(storedFile);

            return new UploadProblemDatasetView(
                    dataset.getDatasetId(),
                    dataset.getOriginalFileName(),
                    dataset.getStoredFileName(),
                    dataset.getFileUrl(),
                    dataset.getFilePath(),
                    dataset.getStatus()
            );
        } catch (Exception e) {
            log.warn("Dataset upload failed. originalFileName={}", command.originalFileName(), e);
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }

    private String startCode(String filePath) {
        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + filePath + "\")";
    }
}
