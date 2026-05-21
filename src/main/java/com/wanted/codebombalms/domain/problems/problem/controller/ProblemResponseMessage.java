package com.wanted.codebombalms.domain.problems.problem.controller;

public class ProblemResponseMessage {

    private ProblemResponseMessage() {}

    public static final String RETRIEVED = "문제 조회에 성공했습니다.";
    public static final String CREATED   = "문제가 생성되었습니다.";
    public static final String UPDATED   = "문제가 수정되었습니다.";
    public static final String DELETED   = "문제가 삭제되었습니다.";
}
