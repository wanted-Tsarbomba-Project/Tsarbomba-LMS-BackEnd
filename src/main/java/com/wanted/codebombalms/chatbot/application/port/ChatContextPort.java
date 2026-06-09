package com.wanted.codebombalms.chatbot.application.port;

import java.util.List;

public interface ChatContextPort {

    // problemSetId로 문제집 정보 조회
    ProblemSetInfo findProblemSet(Long problemSetId);

    // problemSetId + userId로 문제 목록 조회 (submittedAnswer 포함)
    List<ProblemInfo> findProblems(Long problemSetId, Long userId);

    // problemId로 현재 문제 순서 조회
    SessionProgressInfo findSessionProgress(Long problemId);

    // problemId로 문제 제목 조회
    String findProblemTitle(Long problemId);

    // problemSetId로 데이터셋 정보 조회
    DatasetInfo findDataset(Long problemSetId);

    record ProblemSetInfo(
            Long problemSetId,
            String title,
            String description
    ) {}

    record ProblemInfo(
            String title,
            String content,
            String problemType,
            String explanation,
            String submittedAnswer
    ) {}

    record SessionProgressInfo(
            int currentProblemNumber
    ) {}

    record DatasetInfo(
            String metaData
    ) {}
}
