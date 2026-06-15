package com.wanted.codebombalms.chatbot.application.port;

// 채팅방 목록 소제목용: 연결된 문제집/문제 제목 조회 (삭제 시 null)
public interface ProblemTitlePort {
    String findProblemSetTitleOrNull(Long problemSetId);
    String findProblemTitleOrNull(Long problemId);
}
