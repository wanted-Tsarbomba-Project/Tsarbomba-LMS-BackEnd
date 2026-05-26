package com.wanted.codebombalms.course.application.port;

public interface LectureCatalogPort {

    boolean existsLectureInCourse(Long courseId);

    boolean existsLectureInCourse(Long courseId, Long lectureId);
}
