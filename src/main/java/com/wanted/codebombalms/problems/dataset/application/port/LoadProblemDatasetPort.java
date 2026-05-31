package com.wanted.codebombalms.problems.dataset.application.port;

import java.util.Optional;

// 챗봇 adapter용 dataset 조회 포트
public interface LoadProblemDatasetPort {

    Optional<String> findLatestActiveMetadata(Long problemSetId);
}
