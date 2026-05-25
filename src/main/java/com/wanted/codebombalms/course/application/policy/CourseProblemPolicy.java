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
        if (problemSet.problemSetId() == null || problemSet.role() == null) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_SET_REQUIRED);
        }
        if (!problemCatalogPort.existsProblemSet(problemSet.problemSetId())) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_SET_NOT_FOUND);
        }
        if (problemSet.steps() == null || problemSet.steps().isEmpty()) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_STEP_REQUIRED);
        }

        for (ConfigureCourseProblemSetsCommand.ProblemStepCommand step : problemSet.steps()) {
            validateStep(courseId, problemSet, step);
        }
    }

    private void validateStep(
            Long courseId,
            ConfigureCourseProblemSetsCommand.ProblemSetCommand problemSet,
            ConfigureCourseProblemSetsCommand.ProblemStepCommand step
    ) {
        if (step.problemId() == null || step.stepOrder() == null) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_STEP_REQUIRED);
        }
        if (!problemCatalogPort.existsProblemInSet(problemSet.problemSetId(), step.problemId())) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_NOT_FOUND);
        }
        if (problemSet.role() == CourseProblemSetRole.MAIN && step.lectureId() == null) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_LECTURE_REQUIRED);
        }
        if (step.lectureId() != null && !lectureCatalogPort.existsLectureInCourse(courseId, step.lectureId())) {
            throw new ValidationException(CourseErrorCode.COURSE_PROBLEM_LECTURE_NOT_FOUND);
        }
    }
}
