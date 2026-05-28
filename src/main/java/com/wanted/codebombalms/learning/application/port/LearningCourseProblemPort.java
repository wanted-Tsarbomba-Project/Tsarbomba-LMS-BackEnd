package com.wanted.codebombalms.learning.application.port;

import java.util.List;

public interface LearningCourseProblemPort {

    List<Long> findMainLectureProblemSetIdsByCourse(Long courseId);

    List<Long> findLectureProblemSetIdsByLecture(Long lectureId);
}
