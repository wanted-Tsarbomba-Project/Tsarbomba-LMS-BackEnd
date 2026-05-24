package com.wanted.codebombalms.course.application.port;

public interface ProblemCatalogPort {

    boolean existsProblemSet(Long problemSetId);

    boolean existsProblemInSet(Long problemSetId, Long problemId);
}
