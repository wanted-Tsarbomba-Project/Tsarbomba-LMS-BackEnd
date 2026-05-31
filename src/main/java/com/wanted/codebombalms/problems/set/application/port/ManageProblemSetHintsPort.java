package com.wanted.codebombalms.problems.set.application.port;

public interface ManageProblemSetHintsPort {

    void createHint(Long problemId, String hintContent);

    void updateOrCreateHint(Long problemId, Long hintId, String hintContent);
}