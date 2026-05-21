package com.wanted.codebombalms.domain.course.application.usecase;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseDetailResponse;

public interface CourseCommandUseCase {

    CourseDetailResponse createCourse(CreateCourseCommand command);

    CourseDetailResponse updateCourse(UpdateCourseCommand command);

    CourseDetailResponse publishCourse(PublishCourseCommand command);

    void deleteCourse(Long courseId);
}
