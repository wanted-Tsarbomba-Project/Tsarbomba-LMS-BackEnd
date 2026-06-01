package com.wanted.codebombalms.learning.application.port;

public interface LearningLectureProblemSetPort {

    LearningLectureProblemSet findLectureProblemSet(Long lectureProblemSetId);

    boolean existsProblemInSet(Long problemSetId, Long problemId);
}
