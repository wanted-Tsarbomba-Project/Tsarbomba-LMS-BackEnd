package com.wanted.codebombalms.lecture.application.command;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;

public record ConfigureLectureProblemSetsCommand(
        Long courseId,
        List<ProblemSetCommand> problemSets
) {

    public record ProblemSetCommand(
            Long lectureId,
            Long problemSetId,
            LectureProblemSetRole role,
            Integer displayOrder
    ) {
    }
}
