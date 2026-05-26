package com.wanted.codebombalms.learning.application.port;

import java.util.List;

public interface LearningLecturePort {

    boolean existsLecture(Long lectureId);

    List<Long> findLectureIdsByCourse(Long courseId);
}
