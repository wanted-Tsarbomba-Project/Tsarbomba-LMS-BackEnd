package com.wanted.codebombalms.course.application.command;

import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import java.util.List;

public record ConfigureCourseProblemSetsCommand(
        Long courseId,
        List<ProblemSetCommand> problemSets
) {

    public record ProblemSetCommand(
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
    }
}
