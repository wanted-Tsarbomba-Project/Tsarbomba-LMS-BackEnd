package com.wanted.codebombalms.enrollment.application.port;

public interface CourseCatalogPort {

    CoursePublicationStatus getPublicationStatus(Long courseId);
}
