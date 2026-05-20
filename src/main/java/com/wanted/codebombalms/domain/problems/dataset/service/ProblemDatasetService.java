package com.wanted.codebombalms.domain.problems.dataset.service;

import com.wanted.codebombalms.domain.problems.dataset.dto.request.ProblemDatasetConnectRequest;
import com.wanted.codebombalms.domain.problems.dataset.dto.response.ProblemDatasetConnectResponse;
import com.wanted.codebombalms.domain.problems.dataset.dto.response.ProblemDatasetUploadResponse;
import com.wanted.codebombalms.domain.problems.dataset.entitiy.ProblemDataset;
import com.wanted.codebombalms.domain.problems.dataset.repository.ProblemDatasetRepository;
import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.service.ProblemService;
import com.wanted.codebombalms.domain.problems.dataset.storage.DatasetFileStorage;
import com.wanted.codebombalms.domain.problems.dataset.storage.StoredDatasetFile;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ProblemDatasetService {
    private final ProblemDatasetRepository problemDatasetRepository;
    private final ProblemService problemService;
    private final DatasetFileStorage datasetFileStorage;

    @Transactional
    public ProblemDatasetConnectResponse connectDataset(
            Long problemId,
            ProblemDatasetConnectRequest request
    ) {
        Problem problem = problemService.findProblemEntity(problemId);

        ProblemDataset dataset = problemDatasetRepository.findById(request.datasetId())
                .orElseThrow(() -> new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND));

        if (dataset.getProblem() != null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_ALREADY_CONNECTED);
        }

        if (!"CODE".equals(problem.getProblemType())) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_PROBLEM_TYPE);
        }

        dataset.connectProblem(problem);

        String startCode = "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + dataset.getFilePath() + "\")";

        return new ProblemDatasetConnectResponse(
                problem.getProblemId(),
                dataset.getDatasetId(),
                startCode
        );
    }

    @Transactional
    public ProblemDatasetUploadResponse uploadDataset(MultipartFile datasetFile) {
        validateDatasetFile(datasetFile);

        try {
            StoredDatasetFile storedFile = datasetFileStorage.store(datasetFile);

            ProblemDataset dataset = ProblemDataset.createUploaded(
                    storedFile.originalFileName(),
                    storedFile.storedFileName(),
                    storedFile.fileUrl(),
                    storedFile.filePath(),
                    storedFile.fileSize()
            );

            ProblemDataset savedDataset = problemDatasetRepository.save(dataset);

            return new ProblemDatasetUploadResponse(savedDataset);
        } catch (Exception e) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_UPLOAD_FAILED);
        }
    }

    private void validateDatasetFile(MultipartFile datasetFile) {
        if (datasetFile == null || datasetFile.isEmpty()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        String originalFileName = datasetFile.getOriginalFilename();

        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".csv")) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
    }

}
