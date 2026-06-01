package com.wanted.codebombalms.learning.application.port;

import java.util.List;

public interface LearningLecturePort {

    boolean existsLecture(Long lectureId);

    Long findCourseIdByLecture(Long lectureId);

    List<Long> findLectureIdsByCourse(Long courseId);

    List<LearningLecture> findLecturesByCourse(Long courseId);
}
