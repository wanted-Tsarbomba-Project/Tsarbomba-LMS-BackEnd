package com.wanted.codebombalms.problems.set.application.port;

public interface LoadProblemStartCodePort {

    String loadStartCode(Long problemId);

    String loadStartCodeByProblemSetId(Long problemSetId);
}
