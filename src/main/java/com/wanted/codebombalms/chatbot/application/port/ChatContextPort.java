package com.wanted.codebombalms.chatbot.application.port;

import java.util.List;

public interface ChatContextPort {

    // problemSetId로 문제집 정보 조회
    ProblemSetInfo findProblemSet(Long problemSetId);

    // problemId로 문제 정보 조회
    ProblemInfo findProblem(Long problemId);

    // userId + problemId로 최신 제출 내역 조회
    SubmissionInfo findLatestSubmission(Long userId, Long problemId);

    // problemSetId로 세션 진행 정보 조회
    SessionProgressInfo findSessionProgress(Long problemSetId);

    // problemId로 데이터셋 정보 조회
    DatasetInfo findDataset(Long problemId);

    record ProblemSetInfo(
            String title,
            String description,
            String difficulty,
            String categoryName
    ) {}

    record ProblemInfo(
            Long problemId,
            int problemOrder,
            String title,
            String content,
            String problemType,
            String answer,
            String explanation
    ) {}

    record SubmissionInfo(
            String submittedAnswer,
            boolean isCorrect
    ) {}

    record SessionProgressInfo(
            int currentProblemNumber,
            int totalProblemCount,
            List<String> problemTitles
    ) {}

    record DatasetInfo(
            String fileName,
            String fileUrl
    ) {}
}