package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetManagementPort;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemDatasetPersistenceAdapter implements ProblemDatasetManagementPort {

    private final SpringDataProblemRepository problemRepository;
    private final SpringDataProblemDatasetRepository problemDatasetRepository;

    @Override
    public ProblemDatasetConnection connectDataset(ConnectProblemDatasetCommand command) {
        ProblemJpaEntity problem = problemRepository.findById(command.problemId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = problemDatasetRepository.findById(command.datasetId())
                .orElseThrow(() -> new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND));

        validateConnectable(problem, dataset);

        dataset.connectProblem(problem);

        return ProblemDatasetConnection.connect(
                problem.getProblemId(),
                dataset.getDatasetId(),
                dataset.getFilePath()
        );
    }

    @Override
    public ProblemDataset saveUploadedDataset(StoredDatasetFile storedFile) {
        ProblemDatasetJpaEntity dataset = ProblemDatasetJpaEntity.createUploaded(
                storedFile.getOriginalFileName(),
                storedFile.getStoredFileName(),
                storedFile.getFileUrl(),
                storedFile.getFilePath(),
                storedFile.getFileSize()
        );

        ProblemDatasetJpaEntity savedDataset = problemDatasetRepository.save(dataset);

        return ProblemDataset.restore(
                savedDataset.getDatasetId(),
                savedDataset.getOriginalFileName(),
                savedDataset.getStoredFileName(),
                savedDataset.getFileUrl(),
                savedDataset.getFilePath(),
                savedDataset.getStatus()
        );
    }

    private void validateConnectable(ProblemJpaEntity problem, ProblemDatasetJpaEntity dataset) {
        if (dataset.getProblem() != null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_ALREADY_CONNECTED);
        }

        if (!"CODE".equals(problem.getProblemType())) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_PROBLEM_TYPE);
        }
    }
}
