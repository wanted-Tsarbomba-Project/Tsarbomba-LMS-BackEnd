package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionRequest;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionTarget;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;
import com.wanted.codebombalms.problems.dataset.domain.repository.ProblemDatasetRepository;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemDatasetPersistenceAdapter implements ProblemDatasetRepository {

    private final SpringDataProblemRepository problemRepository;
    private final SpringDataProblemDatasetRepository problemDatasetRepository;

    @Override
    public ProblemDatasetConnectionTarget loadConnectionTarget(ProblemDatasetConnectionRequest request) {
        ProblemJpaEntity problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = problemDatasetRepository.findById(request.getDatasetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND));

        Long connectedProblemId = dataset.getProblem() == null
                ? null
                : dataset.getProblem().getProblemId();

        return new ProblemDatasetConnectionTarget(
                problem.getProblemId(),
                problem.getProblemType(),
                dataset.getDatasetId(),
                connectedProblemId,
                dataset.getFileUrl()
        );
    }

    @Override
    public ProblemDatasetConnection connectDataset(ProblemDatasetConnectionRequest request) {
        ProblemJpaEntity problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = problemDatasetRepository.findById(request.getDatasetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND));

        dataset.connectProblem(problem);

        return ProblemDatasetConnection.connect(
                problem.getProblemId(),
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
