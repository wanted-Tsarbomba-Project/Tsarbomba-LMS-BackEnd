package com.wanted.codebombalms.domain.course.application.port;

public interface LectureCatalogPort {

    boolean existsLectureInCourse(Long courseId);
}
