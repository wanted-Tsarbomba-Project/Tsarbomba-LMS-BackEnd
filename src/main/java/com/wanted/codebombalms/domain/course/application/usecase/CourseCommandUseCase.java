package com.wanted.codebombalms.domain.course.application.usecase;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.result.CourseDetailResult;

public interface CourseCommandUseCase {

    CourseDetailResult createCourse(CreateCourseCommand command);

    CourseDetailResult updateCourse(UpdateCourseCommand command);

    CourseDetailResult publishCourse(PublishCourseCommand command);

    void deleteCourse(Long courseId);
}
