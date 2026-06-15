package com.wanted.codebombalms.problems.dataset.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetDownloadUrlPort;
import com.wanted.codebombalms.problems.dataset.application.port.LoadDatasetDownloadInfoPort;
import com.wanted.codebombalms.problems.dataset.application.usecase.IssueDatasetDownloadUrlUseCase;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.usecase.ValidateProblemSetAccessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public
class DatasetDownloadUrlService implements IssueDatasetDownloadUrlUseCase {

    private final LoadDatasetDownloadInfoPort loadDatasetDownloadInfoPort;
    private final GenerateDatasetDownloadUrlPort generateDatasetDownloadUrlPort;
    private final ValidateProblemSetAccessUseCase validateProblemSetAccessUseCase;

    @Override
    public DatasetDownloadUrlResult issueDownloadUrl(
            Long userId,
            Long problemSetId
    ) {
        validateProblemSetId(problemSetId);
        validateProblemSetAccessUseCase.validate(userId, problemSetId);

        var dataset = loadDatasetDownloadInfoPort
                .loadActiveDataset(problemSetId)
                .orElseThrow(() -> new NotFoundException(
                        ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND
                ));

        validateDataset(dataset);

        String downloadUrl = generateDatasetDownloadUrlPort.generate(
                dataset.filePath(),
                dataset.originalFileName()
        );

        return new DatasetDownloadUrlResult(
                dataset.originalFileName(),
                downloadUrl
        );
    }

    private void validateProblemSetId(Long problemSetId) {
        if (problemSetId == null || problemSetId <= 0) {
            throw new ValidationException(
                    ProblemErrorCode.INVALID_INPUT
            );
        }
    }

    private void validateDataset(
            LoadDatasetDownloadInfoPort.DatasetDownloadInfo dataset
    ) {
        if (dataset.originalFileName() == null
                || dataset.originalFileName().isBlank()
                || dataset.filePath() == null
                || dataset.filePath().isBlank()) {
            throw new NotFoundException(
                    ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND
            );
        }
    }
}
