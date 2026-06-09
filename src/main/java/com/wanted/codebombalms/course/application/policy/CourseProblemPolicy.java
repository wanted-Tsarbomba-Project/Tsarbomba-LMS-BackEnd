package com.wanted.codebombalms.course.application.policy;

import com.wanted.codebombalms.course.application.command.ConfigureCourseProblemSetsCommand;
import com.wanted.codebombalms.course.application.port.LectureCatalogPort;
import com.wanted.codebombalms.course.application.port.ProblemCatalogPort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseProblemPolicy {

    private final ProblemCatalogPort problemCatalogPort;
    private final LectureCatalogPort lectureCatalogPort;

    public void validate(ConfigureCourseProblemSetsCommand command) {
        if (command.problemSets() == null || command.problemSets().isEmpty()) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_SET_REQUIRED);
        }

        for (ConfigureCourseProblemSetsCommand.ProblemSetCommand problemSet : command.problemSets()) {
            validateProblemSet(command.courseId(), problemSet);
        }
    }

    private void validateProblemSet(
            Long courseId,
            ConfigureCourseProblemSetsCommand.ProblemSetCommand problemSet
    ) {
        if (problemSet.problemSetId() == null
                || problemSet.role() == null
                || problemSet.displayOrder() == null) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_SET_REQUIRED);
        }
        if (!problemCatalogPort.existsProblemSet(problemSet.problemSetId())) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_SET_NOT_FOUND);
        }
        if (problemSet.role() == CourseProblemSetRole.FINAL) {
            if (problemSet.lectureId() != null) {
                throw new ValidationException(CourseErrorCode.COURSE_FINAL_PROBLEM_LECTURE_NOT_ALLOWED);
            }
            return;
        }
        if (problemSet.lectureId() == null) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_LECTURE_REQUIRED);
        }
        if (!lectureCatalogPort.existsLectureInCourse(courseId, problemSet.lectureId())) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_LECTURE_NOT_FOUND);
        }
    }
}
