package com.wanted.codebombalms.problems.dataset.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.LoadProblemDatasetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemDatasetQueryService {

    private final LoadProblemDatasetPort loadProblemDatasetPort;

    // === 챗봇 adapter용 메서드 ===
    // chatbot ChatContextAdapter.findDataset() 가 호출
    @Transactional(readOnly = true)
    public Optional<String> findLatestActiveMetadata(Long problemSetId) {
        return loadProblemDatasetPort.findLatestActiveMetadata(problemSetId);
    }
}
