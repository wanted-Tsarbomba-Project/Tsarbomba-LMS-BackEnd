package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.course.domain.model.Course;

public interface CourseCommandUseCase {

    Course createCourse(CreateCourseCommand command);

    Course updateCourse(UpdateCourseCommand command);

    Course publishCourse(PublishCourseCommand command);

    void deleteCourse(Long courseId);
}
