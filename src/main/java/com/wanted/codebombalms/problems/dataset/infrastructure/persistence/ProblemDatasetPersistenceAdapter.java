package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.wanted.codebombalms.problems.dataset.application.port.ProblemDatasetPersistencePort;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionRequest;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionTarget;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemDatasetPersistenceAdapter implements
        ProblemDatasetPersistencePort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemDatasetRepository problemDatasetRepository;

    @Override
    public ProblemDatasetConnectionTarget loadConnectionTarget(ProblemDatasetConnectionRequest request) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(request.getProblemSetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = problemDatasetRepository.findById(request.getDatasetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND));

        Long connectedProblemSetId = dataset.getProblemSet() == null
                ? null
                : dataset.getProblemSet().getProblemSetId();

        return new ProblemDatasetConnectionTarget(
                problemSet.getProblemSetId(),
                dataset.getDatasetId(),
                connectedProblemSetId,
                dataset.getFileUrl()
        );
    }

    @Override
    public ProblemDatasetConnection connectDataset(ProblemDatasetConnectionRequest request) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(request.getProblemSetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = problemDatasetRepository.findById(request.getDatasetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND));

        dataset.connectProblemSet(problemSet);

        return ProblemDatasetConnection.connect(
                problemSet.getProblemSetId(),
                dataset.getDatasetId(),
                dataset.getFileUrl()
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

}
